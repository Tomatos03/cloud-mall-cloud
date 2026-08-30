package com.cloudmall.consumer.handler;

import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.consumer.handler.EventHandler;
import com.cloudmall.coupon.api.client.InternalCouponClient;
import com.cloudmall.coupon.api.request.CouponOrderCommitByOrderReq;
import com.cloudmall.order.api.event.OrderEvent;
import com.cloudmall.order.api.event.OrderTagEnum;

/**
 * 订单支付成功处理器：核销订单下全部已锁定用户券。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class OrderPaidHandler implements EventHandler<OrderEvent> {

    private final InternalCouponClient internalCouponClient;

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
    public String eventId(OrderEvent event) {
        return event.getOrderNo();
    }

    /**
     * 核销订单下全部已锁定用户券。
     *
     * @param event 事件
     */
    @Override
    public void handle(OrderEvent event) {
        internalCouponClient.commitByOrder(
                CouponOrderCommitByOrderReq.builder()
                                           .orderNo(event.getOrderNo())
                                           .build()
        );
    }
}
