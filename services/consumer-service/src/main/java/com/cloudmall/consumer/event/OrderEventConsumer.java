package com.cloudmall.consumer.event;

import java.util.List;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.springframework.stereotype.Component;

import com.cloudmall.consumer.consumer.AbstractMessageConsumer;
import com.cloudmall.consumer.handler.EventHandler;
import com.cloudmall.order.api.event.OrderEvent;

/**
 * 订单事件消费者
 * <p>
 * 消费 order-service 发布的订单生命周期事件，按 tag 分发到对应处理器。
 *
 * @author : Tomatos
 */
@Component
@RocketMQMessageListener(
        topic = "ORDER_EVENTS",
        consumerGroup = "coupon-order-events",
        selectorType = SelectorType.TAG,
        selectorExpression = "OrderPaid || OrderTimeout || OrderCancelled || OrderFullyRefunded",
        maxReconsumeTimes = 16
)
public class OrderEventConsumer extends AbstractMessageConsumer<OrderEvent> {

    /**
     * 构造器。
     *
     * @param handlers 订单事件处理器
     */
    public OrderEventConsumer(List<EventHandler<OrderEvent>> handlers) {
        super(handlers, OrderEvent.class);
    }
}
