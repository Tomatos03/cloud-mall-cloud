package com.cloudmall.seckill.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.rocketmq.producer.MQProducer;
import com.cloudmall.seckill.api.event.SeckillOrderCreateEvent;
import com.cloudmall.seckill.api.event.SeckillTagEnum;
import com.cloudmall.seckill.api.response.SeckillOrderResp;
import com.cloudmall.seckill.convert.SeckillConverter;
import com.cloudmall.seckill.entity.SeckillOrderDO;
import com.cloudmall.seckill.mapper.SeckillOrderMapper;
import com.cloudmall.seckill.mapper.SeckillProductMapper;
import com.cloudmall.seckill.service.ISeckillOrderService;

/**
 * 秒杀单服务实现
 * <p>
 * 状态迁移一律走条件更新（WHERE status=...），以影响行数吃掉支付确认与超时任务的竞态：
 * 谁先改状态谁赢，后到者影响行数为 0 直接跳过。
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillOrderService implements ISeckillOrderService {

    /** 秒杀事件 Topic */
    private static final String SECKILL_ORDERS_TOPIC = "SECKILL_ORDERS";

    /** 秒杀单未支付超时时长（分钟） */
    private static final long PAYMENT_TIMEOUT_MINUTES = 15;

    /** 单次超时扫描上限 */
    private static final int TIMEOUT_SCAN_LIMIT = 200;

    private final SeckillOrderMapper orderMapper;
    private final SeckillProductMapper productMapper;
    private final MQProducer mqProducer;
    private final SeckillConverter seckillConverter;

    /**
     * 落库秒杀单并发送建单事务消息。 <p>
     * 本地事务由
     * {@link com.cloudmall.seckill.listener.SeckillTxMessageListener}
     * 执行。
     *
     * @param order 秒杀单实体（
     * activityId/productId/skuId/userId/seckillPrice
     *              必填）
     * @param addressId 收货地址 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createWithEvent(SeckillOrderDO order, Long addressId) {
        // 唯一键 uk_activity_sku_user 冲突时抛 DuplicateKeyException，由编排层回补 Redis
        orderMapper.insert(order);

        // 构建事件并发送事务消息（本地事务由监听器执行）
        SeckillOrderCreateEvent event = SeckillOrderCreateEvent.builder()
                                                               .seckillOrderId(order.getId())
                                                               .orderNo(order.getOrderNo())
                                                               .userId(order.getUserId())
                                                               .skuId(order.getSkuId())
                                                               .productId(order.getProductId())
                                                               .seckillPrice(order.getSeckillPrice())
                                                               .quantity(order.getQuantity())
                                                               .addressId(addressId)
                                                               .build();
        mqProducer.sendInTransaction(
                SECKILL_ORDERS_TOPIC,
                SeckillTagEnum.ORDER_CREATE.getTag(),
                event,
                order.getOrderNo()
        );
        log.info("秒杀单已受理并发送建单事务消息 orderNo={} userId={}", order.getOrderNo(), order.getUserId());
    }

    /**
     * 支付确认：PENDING_PAYMENT → PAID 并落库扣减秒杀池。 <p>
     * 状态条件更新影响行数为 0 时幂等跳过（已支付或已取消），不重复扣减。
     *
     * @param orderNo 真实订单号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPaid(String orderNo) {
        SeckillOrderDO order = Asserts.requireNonNull(
                orderMapper.selectByOrderNo(orderNo),
                BizError.SECKILL_ORDER_NOT_EXIST
        );

        int transitioned = orderMapper.markPaidIfPending(order.getId());
        if (transitioned == 0) {
            log.info("秒杀单状态非待支付，幂等跳过 orderNo={}", orderNo);
            return;
        }

        // 只有状态迁移成功才落库扣减；WHERE stock>=1 兜底防负库存
        int deducted = productMapper.deductStock(order.getActivityId(), order.getSkuId());
        if (deducted == 0) {
            log.warn("秒杀池库存不足，落库扣减失败 orderNo={} skuId={}", orderNo, order.getSkuId());
        }
        log.info("秒杀单支付确认完成 orderNo={}", orderNo);
    }

    /**
     * 扫描超时未支付的秒杀单。
     *
     * @return 待回收的秒杀单列表
     */
    @Override
    public List<SeckillOrderDO> scanTimeoutOrders() {
        return orderMapper.selectTimeoutOrders(
                LocalDateTime.now().minusMinutes(PAYMENT_TIMEOUT_MINUTES),
                TIMEOUT_SCAN_LIMIT
        );
    }

    /**
     * 条件取消秒杀单：PENDING_PAYMENT → CANCELLED。 <p>
     * 先状态后归还，影响行数为 0 说明已被支付确认抢先，调用方不得归还库存。
     *
     * @param seckillOrderId 秒杀单 ID
     * @return true 表示本次调用成功取消
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelIfPending(Long seckillOrderId) {
        return orderMapper.cancelIfPending(seckillOrderId) > 0;
    }

    /**
     * 按真实订单号查询秒杀单。
     *
     * @param orderNo 真实订单号
     * @return 秒杀单实体
     */
    @Override
    public SeckillOrderDO getByOrderNo(String orderNo) {
        return Asserts.requireNonNull(orderMapper.selectByOrderNo(orderNo), BizError.SECKILL_ORDER_NOT_EXIST);
    }

    /**
     * 查询用户的秒杀单（按 ID 倒序）。
     *
     * @param userId 用户 ID
     * @return 秒杀单列表
     */
    @Override
    public List<SeckillOrderResp> listByUserId(Long userId) {
        List<SeckillOrderDO> orders = orderMapper.listByUserId(userId);
        return seckillConverter.toSeckillOrderRespList(orders);
    }
}
