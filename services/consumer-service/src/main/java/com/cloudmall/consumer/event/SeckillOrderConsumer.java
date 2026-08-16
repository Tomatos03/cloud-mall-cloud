package com.cloudmall.consumer.event;

import java.util.List;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.springframework.stereotype.Component;

import com.cloudmall.rocketmq.consumer.AbstractMessageConsumer;
import com.cloudmall.rocketmq.consumer.IMessageConsumeService;
import com.cloudmall.rocketmq.handler.EventHandler;
import com.cloudmall.seckill.api.event.SeckillOrderCreateEvent;

/**
 * 秒杀事件消费者
 * <p>
 * 消费 seckill-service 发布的抢购事件，驱动 order-service 异步建真实订单。
 *
 * @author : Tomatos
 */
@Component
@RocketMQMessageListener(
        topic = "SECKILL_ORDERS",
        consumerGroup = "seckill-order-events",
        selectorType = SelectorType.TAG,
        selectorExpression = "SeckillOrderCreate"
)
public class SeckillOrderConsumer extends AbstractMessageConsumer<SeckillOrderCreateEvent> {

    /**
     * 构造器。
     *
     * @param consumeService 消费幂等服务
     * @param handlers 秒杀事件处理器
     */
    public SeckillOrderConsumer(
        IMessageConsumeService consumeService,
        List<EventHandler<SeckillOrderCreateEvent>> handlers
    ) {
        super(consumeService, handlers, SeckillOrderCreateEvent.class);
    }
}
