package com.cloudmall.user.api.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册事件
 * <p>
 * user-service 注册通过 RocketMQ 事务消息发布，
 * coupon-service 消费后幂等发放新人券。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {

    /** 新注册用户 ID */
    private Long userId;

    /** 用户名 */
    private String username;
}
