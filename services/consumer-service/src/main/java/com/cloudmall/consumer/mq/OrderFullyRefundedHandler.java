package com.cloudmall.consumer.mq;

import java.util.Map;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.consumer.enums.OperationTypeEnum;
import com.cloudmall.coupon.api.client.CouponOrderClient;
import com.cloudmall.coupon.api.request.CouponOrderRevertByOrderReq;
import com.cloudmall.order.api.event.OrderEvent;
import com.cloudmall.order.api.event.OrderTagEnum;
import com.cloudmall.rocketmq.consumer.EventHandler;

/**
 * 订单全额退款处理器：回退订单下全部已核销用户券。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class OrderFullyRefundedHandler implements EventHandler<OrderEvent> {

    private final CouponOrderClient couponOrderClient;

    /**
     * 注册的 tag 映射。
     *
     * @return tag 映射
     */
    @Override
    public Map<String, String> tags() {
        return Map.of(OrderTagEnum.FULLY_REFUNDED.getTag(), OperationTypeEnum.ORDER_REFUNDED.getCode());
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
     * 回退订单下全部已核销用户券（无退款单跳过）。
     *
     * @param event 事件
     */
    @Override
    public void handle(OrderEvent event) {
        if (StrUtil.isBlank(event.getRefundId())) {
            return;
        }
        couponOrderClient.revertByOrder(
                CouponOrderRevertByOrderReq.builder()
                                           .orderNo(event.getOrderNo())
                                           .refundId(event.getRefundId())
                                           .build()
        );
    }
}
