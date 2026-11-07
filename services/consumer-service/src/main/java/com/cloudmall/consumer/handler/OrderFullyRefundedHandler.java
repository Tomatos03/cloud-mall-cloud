package com.cloudmall.consumer.handler;

import java.util.Set;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.consumer.handler.IEventHandler;
import com.cloudmall.coupon.api.client.InternalCouponClient;
import com.cloudmall.coupon.api.model.req.CouponOrderRevertByOrderReq;
import com.cloudmall.order.api.model.bo.OrderEvent;
import com.cloudmall.order.api.enums.OrderTagEnum;

/**
 * 订单全额退款处理器：回退订单下全部已核销用户券。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class OrderFullyRefundedHandler implements IEventHandler<OrderEvent> {

    private final InternalCouponClient internalCouponClient;

    /**
     * 本处理器服务的 RocketMQ tag 集合。
     *
     * @return tag 集合
     */
    @Override
    public Set<String> tagSet() {
        return Set.of(OrderTagEnum.FULLY_REFUNDED.getTag());
    }

    /**
     * 判断事件是否由本处理器处理。
     *
     * @param tag 消息 tag
     * @return true 表示全额退款事件
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
     * 回退订单下全部已核销用户券（无退款单跳过）。
     *
     * @param event 事件
     */
    @Override
    public void handle(OrderEvent event) {
        if (StrUtil.isBlank(event.getRefundId())) {
            return;
        }
        internalCouponClient.revertByOrder(
                CouponOrderRevertByOrderReq.builder()
                                           .orderNo(event.getOrderNo())
                                           .refundId(event.getRefundId())
                                           .build()
        );
    }
}
