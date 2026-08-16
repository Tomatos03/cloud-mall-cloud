package com.cloudmall.consumer.event;

import java.util.List;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.springframework.stereotype.Component;

import com.cloudmall.rocketmq.consumer.AbstractMessageConsumer;
import com.cloudmall.rocketmq.consumer.IMessageConsumeService;
import com.cloudmall.rocketmq.handler.EventHandler;
import com.cloudmall.seckill.api.event.SeckillPaidEvent;

/**
 * 秒杀支付确认消费者
 * <p>
 * 独立消费组订阅 ORDER_EVENTS 的 OrderPaid：与券核销消费组各自收到一份消息，
 * 互不抢占（消费模板对同一 tag 只分发首个匹配处理器）。
 *
 * @author : Tomatos
 */
@Component
@RocketMQMessageListener(
        topic = "ORDER_EVENTS",
        consumerGroup = "seckill-order-paid",
        selectorType = SelectorType.TAG,
        selectorExpression = "OrderPaid"
)
public class SeckillOrderPaidConsumer extends AbstractMessageConsumer<SeckillPaidEvent> {

    /**
     * 构造器。
     *
     * @param consumeService 消费幂等服务
     * @param handlers 秒杀支付事件处理器
     */
    public SeckillOrderPaidConsumer(
        IMessageConsumeService consumeService,
        List<EventHandler<SeckillPaidEvent>> handlers
    ) {
        super(consumeService, handlers, SeckillPaidEvent.class);
    }
}
