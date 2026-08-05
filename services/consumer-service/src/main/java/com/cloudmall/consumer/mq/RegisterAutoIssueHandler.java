package com.cloudmall.consumer.mq;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.auth.api.event.UserRegisteredEvent;
import com.cloudmall.consumer.enums.OperationTypeEnum;
import com.cloudmall.coupon.api.client.UserCouponInternalClient;
import com.cloudmall.coupon.api.request.AutoIssueRegisterReq;
import com.cloudmall.rocketmq.consumer.EventHandler;

/**
 * 用户注册自动发券处理器。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class RegisterAutoIssueHandler implements EventHandler<UserRegisteredEvent> {

    private final UserCouponInternalClient userCouponInternalClient;

    /**
     * 注册的 tag 映射（注册消息无 tag，使用空串注册键）。
     *
     * @return tag 映射
     */
    @Override
    public Map<String, String> tags() {
        return Map.of("", OperationTypeEnum.REGISTER_AUTO_ISSUE.getCode());
    }

    /**
     * 提取幂等键。
     *
     * @param event 事件
     * @return 事件 ID
     */
    @Override
    public String eventId(UserRegisteredEvent event) {
        return event.getEventId();
    }

    /**
     * 注册自动发放新人券。
     *
     * @param event 事件
     */
    @Override
    public void handle(UserRegisteredEvent event) {
        userCouponInternalClient.autoIssueForRegister(
                AutoIssueRegisterReq.builder()
                                    .userId(event.getUserId())
                                    .eventId(event.getEventId())
                                    .build()
        );
    }
}
