package com.cloudmall.consumer.handler;

import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cloudmall.order.api.event.OrderTagEnum;
import com.cloudmall.rocketmq.handler.EventHandler;
import com.cloudmall.seckill.api.client.InternalSeckillClient;
import com.cloudmall.seckill.api.event.SeckillPaidEvent;
import com.cloudmall.seckill.api.request.SeckillConfirmPaidReq;

/**
 * 秒杀支付确认处理器
 * <p>
 * 回调秒杀侧确认支付：秒杀单置 PAID 并落库扣减秒杀池。
 * 秒杀单订单号由秒杀侧以 SK 前缀预生成，非秒杀单直接跳过，避免无谓的跨服务调用。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderPaidHandler implements EventHandler<SeckillPaidEvent> {

    /** 秒杀订单号前缀 */
    private static final String SECKILL_ORDER_NO_PREFIX = "SK";

    private final InternalSeckillClient internalSeckillClient;

    /**
     * 本处理器服务的 RocketMQ tag 集合。
     *
     * @return tag 集合
     */
    @Override
    public Set<String> tagSet() {
        return Set.of(OrderTagEnum.PAID.getTag());
    }

    /**
     * 判断事件是否由本处理器处理。
     *
     * @param tag 消息 tag
     * @return true 表示支付成功事件
     */
    @Override
    public boolean canHandle(String tag) {
        return tagSet().contains(tag);
    }

    /**
     * 提取幂等键。
     *
     * @param event 事件
     * @return 事件 ID
     */
    @Override
    public String eventId(SeckillPaidEvent event) {
        return event.getEventId();
    }

    /**
     * 回调秒杀侧确认支付。
     *
     * @param event 事件
     */
    @Override
    public void handle(SeckillPaidEvent event) {
        String orderNo = event.getOrderNo();
        if (orderNo == null || !orderNo.startsWith(SECKILL_ORDER_NO_PREFIX)) {
            return;
        }
        internalSeckillClient.confirmPaid(SeckillConfirmPaidReq.builder()
                                                               .orderNo(orderNo)
                                                               .build());
        log.info("秒杀单支付确认已回调 orderNo={}", orderNo);
    }
}
