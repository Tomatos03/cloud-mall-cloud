package com.cloudmall.outbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Outbox 追加请求
 * <p>
 * 业务服务在本地事务内构造并调用 {@code OutboxAppender.append}。
 * 事件对象必须包含 eventId 字段（消费端幂等键），由追加器生成并注入。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxAppendReq {

    /** 目标 RocketMQ Topic */
    private String topic;

    /** 事件类型（同时作为 RocketMQ tag） */
    private String eventType;

    /** 业务类型（如 ORDER） */
    private String bizType;

    /** 业务 ID（如父订单号） */
    private String bizId;

    /** 事件对象（需含 eventId 字段，由追加器生成并注入） */
    private Object event;
}
