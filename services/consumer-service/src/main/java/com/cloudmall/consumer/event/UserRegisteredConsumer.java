package com.cloudmall.consumer.event;

import java.util.List;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import com.cloudmall.rocketmq.consumer.AbstractMessageConsumer;
import com.cloudmall.rocketmq.consumer.IMessageConsumeService;
import com.cloudmall.rocketmq.handler.EventHandler;
import com.cloudmall.user.api.event.UserRegisteredEvent;

/**
 * 用户注册事件消费者
 * <p>
 * 消费 user-service 发布的 UserRegistered，分发到注册自动发券处理器。
 *
 * @author : Tomatos
 */
@Component
@RocketMQMessageListener(topic = "USER_REGISTERED", consumerGroup = "coupon-user-registered")
public class UserRegisteredConsumer extends AbstractMessageConsumer<UserRegisteredEvent> {

    /**
     * 构造器。
     *
     * @param consumeService 消费幂等服务
     * @param handlers 注册事件处理器
     */
    public UserRegisteredConsumer(
        IMessageConsumeService consumeService,
        List<EventHandler<UserRegisteredEvent>> handlers
    ) {
        super(consumeService, handlers, UserRegisteredEvent.class);
    }
}
