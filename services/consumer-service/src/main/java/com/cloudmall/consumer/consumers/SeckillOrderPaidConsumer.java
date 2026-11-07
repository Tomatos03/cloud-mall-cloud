package com.cloudmall.consumer.consumers;

import java.util.List;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.springframework.stereotype.Component;

import com.cloudmall.consumer.handler.IEventHandler;
import com.cloudmall.seckill.api.model.bo.SeckillPaidEvent;

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
        selectorExpression = "OrderPaid",
        maxReconsumeTimes = 16
)
public class SeckillOrderPaidConsumer extends AbstractMessageConsumer<SeckillPaidEvent> {

    /**
     * 构造器。
     *
     * @param handlers 秒杀支付事件处理器
     */
    public SeckillOrderPaidConsumer(List<IEventHandler<SeckillPaidEvent>> handlers) {
        super(handlers, SeckillPaidEvent.class);
    }
}
