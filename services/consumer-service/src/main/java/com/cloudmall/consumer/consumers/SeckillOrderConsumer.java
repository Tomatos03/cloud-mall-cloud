package com.cloudmall.consumer.consumers;

import java.util.List;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.springframework.stereotype.Component;

import com.cloudmall.consumer.handler.IEventHandler;
import com.cloudmall.seckill.api.model.bo.SeckillOrderCreateEvent;

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
        selectorExpression = "SeckillOrderCreate",
        maxReconsumeTimes = 16
)
public class SeckillOrderConsumer extends AbstractMessageConsumer<SeckillOrderCreateEvent> {

    /**
     * 构造器。
     *
     * @param handlers 秒杀事件处理器
     */
    public SeckillOrderConsumer(List<IEventHandler<SeckillOrderCreateEvent>> handlers) {
        super(handlers, SeckillOrderCreateEvent.class);
    }
}
