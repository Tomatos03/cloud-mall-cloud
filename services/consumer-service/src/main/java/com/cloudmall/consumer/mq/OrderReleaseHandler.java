package com.cloudmall.consumer.mq;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.consumer.enums.OperationTypeEnum;
import com.cloudmall.coupon.api.client.CouponOrderClient;
import com.cloudmall.coupon.api.request.CouponOrderReleaseByOrderReq;
import com.cloudmall.order.api.event.OrderEvent;
import com.cloudmall.order.api.event.OrderTagEnum;
import com.cloudmall.rocketmq.consumer.EventHandler;

/**
 * 订单释放处理器：超时/取消共享同一动作，注册两个 tag。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class OrderReleaseHandler implements EventHandler<OrderEvent> {

    private final CouponOrderClient couponOrderClient;

    /**
     * 注册的 tag 映射。
     *
     * @return tag 映射
     */
    @Override
    public Map<String, String> tags() {
        return Map.of(
                OrderTagEnum.TIMEOUT.getTag(),
                OperationTypeEnum.ORDER_TIMEOUT.getCode(),
                OrderTagEnum.CANCELLED.getTag(),
                OperationTypeEnum.ORDER_CANCELLED.getCode()
        );
    }

    /**
     * 提取幂等键。
     *
     * @param event 事件
     * @return 事件 ID
     */
    @Override
    public String eventId(OrderEvent event) {
        return event.getEventId();
    }

    /**
     * 释放订单下全部已锁定用户券。
     *
     * @param event 事件
     */
    @Override
    public void handle(OrderEvent event) {
        couponOrderClient.releaseByOrder(
                CouponOrderReleaseByOrderReq.builder()
                                            .orderNo(event.getOrderNo())
                                            .userId(event.getUserId())
                                            .build()
        );
    }
}
