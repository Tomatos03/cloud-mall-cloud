package com.cloudmall.consumer.mq;

import java.util.List;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.springframework.stereotype.Component;

import com.cloudmall.order.api.event.OrderEvent;
import com.cloudmall.rocketmq.consumer.AbstractMessageConsumer;
import com.cloudmall.rocketmq.consumer.EventHandler;
import com.cloudmall.rocketmq.consumer.IMessageConsumeService;

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
        selectorExpression = "OrderPaid || OrderTimeout || OrderCancelled || OrderFullyRefunded"
)
public class OrderEventConsumer extends AbstractMessageConsumer<OrderEvent> {

    /**
     * 构造器。
     *
     * @param consumeService 消费幂等服务
     * @param handlers 订单事件处理器
     */
    public OrderEventConsumer(IMessageConsumeService consumeService, List<EventHandler<OrderEvent>> handlers) {
        super(consumeService, handlers, OrderEvent.class);
    }
}
