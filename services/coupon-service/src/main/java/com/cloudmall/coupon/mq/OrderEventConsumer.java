package com.cloudmall.coupon.mq;

import java.nio.charset.StandardCharsets;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import com.cloudmall.coupon.entity.UserCouponDO;
import com.cloudmall.coupon.enums.UserCouponStatusEnum;
import com.cloudmall.coupon.mapper.UserCouponMapper;
import com.cloudmall.coupon.service.IMessageConsumeService;
import com.cloudmall.coupon.service.IUserCouponService;
import com.cloudmall.order.api.event.OrderEvent;

/**
 * 订单事件消费者
 * <p>
 * 消费 order-service 发布的订单生命周期事件：
 * OrderPaid 核销、OrderTimeout/OrderCancelled 释放、OrderFullyRefunded 回退。
 * 业务幂等（per-coupon 流水）+ 消费记录兜底，失败抛异常触发重试。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "ORDER_EVENTS",
        consumerGroup = "coupon-order-events",
        selectorType = SelectorType.TAG,
        selectorExpression = "OrderPaid || OrderTimeout || OrderCancelled || OrderFullyRefunded"
)
public class OrderEventConsumer implements RocketMQListener<MessageExt> {

    private static final String TAG_PAID = "OrderPaid";
    private static final String TAG_TIMEOUT = "OrderTimeout";
    private static final String TAG_CANCELLED = "OrderCancelled";
    private static final String TAG_FULLY_REFUNDED = "OrderFullyRefunded";

    private final IMessageConsumeService consumeService;
    private final IUserCouponService userCouponService;
    private final UserCouponMapper userCouponMapper;

    /**
     * 处理订单事件：按 tag 分发到核销/释放/回退。
     *
     * @param message 消息
     */
    @Override
    public void onMessage(MessageExt message) {
        OrderEvent event = parse(message);
        if (event == null || StrUtil.isBlank(event.getEventId())) {
            return;
        }
        String tag = message.getTags();
        if (TAG_PAID.equals(tag)) {
            handlePaid(event);
        } else if (TAG_TIMEOUT.equals(tag)) {
            handleTimeout(event);
        } else if (TAG_CANCELLED.equals(tag)) {
            handleCancelled(event);
        } else if (TAG_FULLY_REFUNDED.equals(tag)) {
            handleFullyRefunded(event);
        }
    }

    /**
     * 支付成功：核销订单下全部已锁定用户券。
     *
     * @param event 订单事件
     */
    private void handlePaid(OrderEvent event) {
        if (consumeService.alreadyConsumed(event.getEventId(), IMessageConsumeService.OP_ORDER_PAID)) {
            return;
        }
        try {
            List<UserCouponDO> locked = listByOrder(event.getOrderNo(), UserCouponStatusEnum.LOCKED);
            if (CollUtil.isNotEmpty(locked)) {
                for (UserCouponDO uc : locked) {
                    userCouponService.commit(event.getOrderNo(), uc.getUserId(), uc.getId());
                }
            }
            consumeService.markConsumed(event.getEventId(), IMessageConsumeService.OP_ORDER_PAID);
        } catch (RuntimeException e) {
            log.error("订单支付核销失败 orderNo={}", event.getOrderNo(), e);
            throw e;
        }
    }

    /**
     * 支付超时：释放订单下全部已锁定用户券。
     *
     * @param event 订单事件
     */
    private void handleTimeout(OrderEvent event) {
        if (consumeService.alreadyConsumed(event.getEventId(), IMessageConsumeService.OP_ORDER_TIMEOUT)) {
            return;
        }
        try {
            releaseLockedByOrder(event);
            consumeService.markConsumed(event.getEventId(), IMessageConsumeService.OP_ORDER_TIMEOUT);
        } catch (RuntimeException e) {
            log.error("订单超时释放失败 orderNo={}", event.getOrderNo(), e);
            throw e;
        }
    }

    /**
     * 订单取消：释放订单下全部已锁定用户券。
     *
     * @param event 订单事件
     */
    private void handleCancelled(OrderEvent event) {
        if (consumeService.alreadyConsumed(event.getEventId(), IMessageConsumeService.OP_ORDER_CANCELLED)) {
            return;
        }
        try {
            releaseLockedByOrder(event);
            consumeService.markConsumed(event.getEventId(), IMessageConsumeService.OP_ORDER_CANCELLED);
        } catch (RuntimeException e) {
            log.error("订单取消释放失败 orderNo={}", event.getOrderNo(), e);
            throw e;
        }
    }

    /**
     * 全额退款：回退订单下全部已核销用户券。
     *
     * @param event 订单事件
     */
    private void handleFullyRefunded(OrderEvent event) {
        if (StrUtil.isBlank(event.getRefundId())) {
            return;
        }
        if (consumeService.alreadyConsumed(event.getEventId(), IMessageConsumeService.OP_ORDER_REFUNDED)) {
            return;
        }
        try {
            List<UserCouponDO> used = listByOrder(event.getOrderNo(), UserCouponStatusEnum.USED);
            if (CollUtil.isNotEmpty(used)) {
                for (UserCouponDO uc : used) {
                    userCouponService.revert(
                            event.getOrderNo(),
                            uc.getUserId(),
                            uc.getId(),
                            event.getRefundId(),
                            "全额退款回退"
                    );
                }
            }
            consumeService.markConsumed(event.getEventId(), IMessageConsumeService.OP_ORDER_REFUNDED);
        } catch (RuntimeException e) {
            log.error("订单全额退款回退失败 orderNo={}", event.getOrderNo(), e);
            throw e;
        }
    }

    /**
     * 释放订单下全部已锁定用户券。
     *
     * @param event 订单事件
     */
    private void releaseLockedByOrder(OrderEvent event) {
        List<UserCouponDO> locked = listByOrder(event.getOrderNo(), UserCouponStatusEnum.LOCKED);
        if (CollUtil.isEmpty(locked)) {
            return;
        }
        for (UserCouponDO uc : locked) {
            userCouponService.release(event.getOrderNo(), uc.getUserId(), uc.getId(), "订单取消/超时释放");
        }
    }

    /**
     * 按订单号与状态查询用户券。
     *
     * @param orderNo 订单号
     * @param status 用户券状态
     * @return 用户券列表
     */
    private List<UserCouponDO> listByOrder(String orderNo, UserCouponStatusEnum status) {
        return userCouponMapper.selectList(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getLockedOrderNo, orderNo)
                        .eq(UserCouponDO::getStatus, status)
        );
    }

    /**
     * 解析消息体为订单事件。
     *
     * @param message 消息
     * @return 订单事件（解析失败返回 null）
     */
    private OrderEvent parse(MessageExt message) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            return JSONUtil.toBean(body, OrderEvent.class);
        } catch (Exception e) {
            log.error("订单事件消息解析失败 msgId={}", message.getMsgId(), e);
            return null;
        }
    }
}
