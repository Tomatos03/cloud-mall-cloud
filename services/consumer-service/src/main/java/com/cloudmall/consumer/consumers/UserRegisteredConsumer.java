package com.cloudmall.consumer.consumers;

import java.util.List;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import com.cloudmall.consumer.handler.IEventHandler;
import com.cloudmall.user.api.model.bo.UserRegisteredEvent;

/**
 * 用户注册事件消费者
 * <p>
 * 消费 user-service 发布的 UserRegistered，分发到注册自动发券处理器。
 *
 * @author : Tomatos
 */
@Component
@RocketMQMessageListener(
        topic = "USER_REGISTERED",
        consumerGroup = "coupon-user-registered",
        maxReconsumeTimes = 16
)
public class UserRegisteredConsumer extends AbstractMessageConsumer<UserRegisteredEvent> {

    /**
     * 构造器。
     *
     * @param handlers 注册事件处理器
     */
    public UserRegisteredConsumer(List<IEventHandler<UserRegisteredEvent>> handlers) {
        super(handlers, UserRegisteredEvent.class);
    }
}
