package com.cloudmall.auth.api.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册事件
 * <p>
 * auth-service 注册事务写入本地 Outbox，由 RocketMQ 发布，
 * coupon-service 消费后幂等发放新人券。eventId 为跨服务幂等键。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {

    /** 事件 ID（Outbox event_id，消费幂等键） */
    private String eventId;

    /** 新注册用户 ID */
    private Long userId;

    /** 用户名 */
    private String username;
}
