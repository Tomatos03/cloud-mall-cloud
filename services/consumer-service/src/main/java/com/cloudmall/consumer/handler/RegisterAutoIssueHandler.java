package com.cloudmall.consumer.handler;

import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.consumer.handler.IEventHandler;
import com.cloudmall.coupon.api.client.InternalCouponClient;
import com.cloudmall.coupon.api.model.req.AutoIssueRegisterReq;
import com.cloudmall.user.api.model.bo.UserRegisteredEvent;

/**
 * 用户注册自动发券处理器。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class RegisterAutoIssueHandler implements IEventHandler<UserRegisteredEvent> {

    private final InternalCouponClient internalCouponClient;

    /**
     * 本处理器服务的 RocketMQ tag 集合。
     *
     * @return tag 集合
     */
    @Override
    public Set<String> tagSet() {
        return Set.of("UserRegistered");
    }

    /**
     * 判断事件是否由本处理器处理。
     *
     * @param tag 消息 tag
     * @return true 表示注册事件
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
    public String eventId(UserRegisteredEvent event) {
        return String.valueOf(event.getUserId());
    }

    /**
     * 注册自动发放新人券。
     *
     * @param event 事件
     */
    @Override
    public void handle(UserRegisteredEvent event) {
        internalCouponClient.autoIssueForRegister(
                AutoIssueRegisterReq.builder()
                                    .userId(event.getUserId())
                                    .eventId(String.valueOf(event.getUserId()))
                                    .build()
        );
    }
}
