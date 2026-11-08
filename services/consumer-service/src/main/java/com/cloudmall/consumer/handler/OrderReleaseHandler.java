package com.cloudmall.consumer.handler;

import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.consumer.handler.IEventHandler;
import com.cloudmall.coupon.api.client.InternalCouponClient;
import com.cloudmall.coupon.api.model.req.CouponOrderReleaseByOrderReq;
import com.cloudmall.order.api.enums.OrderTagEnum;
import com.cloudmall.order.api.model.bo.OrderEvent;

/**
 * 订单释放处理器：超时/取消共享同一动作，注册两个 tag。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class OrderReleaseHandler implements IEventHandler<OrderEvent> {

    private final InternalCouponClient internalCouponClient;

    /**
     * 本处理器服务的 RocketMQ tag 集合。
     *
     * @return tag 集合
     */
    @Override
    public Set<String> tagSet() {
        return Set.of(OrderTagEnum.TIMEOUT.getTag(), OrderTagEnum.CANCELLED.getTag());
    }

    /**
     * 判断事件是否由本处理器处理。
     *
     * @param tag 消息 tag
     * @return true 表示超时或取消事件
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
     * 释放订单下全部已锁定用户券。
     *
     * @param event 事件
     */
    @Override
    public void handle(OrderEvent event) {
        internalCouponClient.releaseByOrder(
                CouponOrderReleaseByOrderReq.builder()
                                            .orderNo(event.getOrderNo())
                                            .userId(event.getUserId())
                                            .build()
        );
    }
}
