package com.cloudmall.order.service.impl;

import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.api.event.OrderEvent;
import com.cloudmall.order.api.event.OrderTagEnum;
import com.cloudmall.order.entity.OrderDO;
import com.cloudmall.order.enums.ParentOrderStatusEnum;
import com.cloudmall.order.mapper.OrderMapper;
import com.cloudmall.order.service.IOrderEventService;
import com.cloudmall.order.service.IOrderStateMachineService;
import com.cloudmall.outbox.appender.OutboxAppender;
import com.cloudmall.outbox.model.OutboxAppendReq;

/**
 * 订单事件服务实现
 * <p>
 * 支付/超时/取消/退款事件（本阶段为模拟入口）：
 * 校验状态迁移并更新父订单，同时写入事件 Outbox（同事务）。
 * 事件由 Outbox 发布任务异步发送，coupon-service 消费后核销/释放/回退用户券。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class OrderEventService implements IOrderEventService {

    private final OrderMapper orderMapper;
    private final IOrderStateMachineService stateMachine;
    private final OutboxAppender outboxAppender;

    /** 订单事件 Topic */
    private static final String ORDER_EVENTS_TOPIC = "ORDER_EVENTS";

    /**
     * 支付成功：将第三方回调金额经模拟转换后与订单应付金额比对，一致才 PENDING_PAYMENT →
     * PAID。
     *
     * @param orderNo 父订单号
     * @param payAmount 第三方回调支付金额
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaid(String orderNo, BigDecimal payAmount) {
        Asserts.notNull(payAmount, BizError.INVALID_PARAM);
        OrderDO order = requireOrder(orderNo);
        BigDecimal convertedAmount = convertThirdPartyAmount(payAmount);
        Asserts.isTrue(
                convertedAmount.compareTo(order.getPayableAmount()) == 0,
                BizError.ORDER_PAY_AMOUNT_MISMATCH
        );
        stateMachine.transitionParent(
                order.getId(),
                ParentOrderStatusEnum.PENDING_PAYMENT,
                ParentOrderStatusEnum.PAID
        );
        OrderEvent event = OrderEvent.builder()
                                     .orderNo(orderNo)
                                     .userId(order.getUserId())
                                     .build();
        outboxAppender.append(
                OutboxAppendReq.builder()
                               .topic(ORDER_EVENTS_TOPIC)
                               .eventType(OrderTagEnum.PAID.getTag())
                               .bizType("ORDER")
                               .bizId(orderNo)
                               .event(event)
                               .build()
        );
    }

    /**
     * 支付超时：PENDING_PAYMENT → TIMEOUT。
     *
     * @param orderNo 父订单号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleTimeout(String orderNo) {
        OrderDO order = requireOrder(orderNo);
        stateMachine.transitionParent(
                order.getId(),
                ParentOrderStatusEnum.PENDING_PAYMENT,
                ParentOrderStatusEnum.TIMEOUT
        );
        OrderEvent event = OrderEvent.builder()
                                     .orderNo(orderNo)
                                     .userId(order.getUserId())
                                     .build();
        outboxAppender.append(
                OutboxAppendReq.builder()
                               .topic(ORDER_EVENTS_TOPIC)
                               .eventType(OrderTagEnum.TIMEOUT.getTag())
                               .bizType("ORDER")
                               .bizId(orderNo)
                               .event(event)
                               .build()
        );
    }

    /**
     * 订单取消：待支付父订单整体取消（CREATING 或 PENDING_PAYMENT）。
     *
     * @param orderNo 父订单号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleCancel(String orderNo) {
        OrderDO order = requireOrder(orderNo);
        int updated = orderMapper.markCancelled(order.getId(), ParentOrderStatusEnum.CANCELLED);
        Asserts.isTrue(updated > 0, BizError.INVALID_ORDER_STATUS);
        OrderEvent event = OrderEvent.builder()
                                     .orderNo(orderNo)
                                     .userId(order.getUserId())
                                     .build();
        outboxAppender.append(
                OutboxAppendReq.builder()
                               .topic(ORDER_EVENTS_TOPIC)
                               .eventType(OrderTagEnum.CANCELLED.getTag())
                               .bizType("ORDER")
                               .bizId(orderNo)
                               .event(event)
                               .build()
        );
    }

    /**
     * 全额退款：校验第三方回调金额与订单应付金额一致后 PAID/PARTIAL_REFUNDED →
     * REFUNDED。
     *
     * @param orderNo 父订单号
     * @param refundId 退款单 ID
     * @param refundAmount 第三方回调退款金额
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleFullyRefunded(String orderNo, String refundId, BigDecimal refundAmount) {
        Asserts.notBlank(refundId, BizError.INVALID_PARAM);
        Asserts.notNull(refundAmount, BizError.INVALID_PARAM);
        OrderDO order = requireOrder(orderNo);
        BigDecimal convertedAmount = convertThirdPartyAmount(refundAmount);
        Asserts.isTrue(
                convertedAmount.compareTo(order.getPayableAmount()) == 0,
                BizError.ORDER_REFUND_AMOUNT_MISMATCH
        );
        int updated = orderMapper.markRefunded(order.getId());
        Asserts.isTrue(updated > 0, BizError.INVALID_ORDER_STATUS);
        OrderTagEnum eventType = OrderTagEnum.FULLY_REFUNDED;
        OrderEvent event = OrderEvent.builder()
                                     .orderNo(orderNo)
                                     .userId(order.getUserId())
                                     .refundId(refundId)
                                     .build();
        outboxAppender.append(
                OutboxAppendReq.builder()
                               .topic(ORDER_EVENTS_TOPIC)
                               .eventType(eventType.getTag())
                               .bizType("ORDER")
                               .bizId(orderNo)
                               .event(event)
                               .build()
        );
    }

    /**
     * 按订单号查询父订单。
     *
     * @param orderNo 父订单号
     * @return 父订单
     */
    private OrderDO requireOrder(String orderNo) {
        OrderDO order = Asserts.requireNonNull(orderMapper.selectByOrderNo(orderNo), BizError.ORDER_NOT_EXIST);
        return order;
    }

    /**
     * 模拟第三方金额到内部金额单位的转换。 <p>
     * 当前未实现真实单位换算，原样返回入参；接入真实支付渠道后在此补充转换逻辑。
     *
     * @param amount 第三方回调金额
     * @return 转换后的内部金额
     */
    private BigDecimal convertThirdPartyAmount(BigDecimal amount) {
        return amount;
    }
}
