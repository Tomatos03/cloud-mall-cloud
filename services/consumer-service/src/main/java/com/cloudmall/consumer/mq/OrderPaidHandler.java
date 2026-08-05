package com.cloudmall.consumer.mq;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.consumer.enums.OperationTypeEnum;
import com.cloudmall.coupon.api.client.CouponOrderClient;
import com.cloudmall.coupon.api.request.CouponOrderCommitByOrderReq;
import com.cloudmall.order.api.event.OrderEvent;
import com.cloudmall.order.api.event.OrderTagEnum;
import com.cloudmall.rocketmq.consumer.EventHandler;

/**
 * 订单支付成功处理器：核销订单下全部已锁定用户券。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class OrderPaidHandler implements EventHandler<OrderEvent> {

    private final CouponOrderClient couponOrderClient;

    /**
     * 注册的 tag 映射。
     *
     * @return tag 映射
     */
    @Override
    public Map<String, String> tags() {
        return Map.of(OrderTagEnum.PAID.getTag(), OperationTypeEnum.ORDER_PAID.getCode());
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
     * 核销订单下全部已锁定用户券。
     *
     * @param event 事件
     */
    @Override
    public void handle(OrderEvent event) {
        couponOrderClient.commitByOrder(
                CouponOrderCommitByOrderReq.builder()
                                           .orderNo(event.getOrderNo())
                                           .build()
        );
    }
}
