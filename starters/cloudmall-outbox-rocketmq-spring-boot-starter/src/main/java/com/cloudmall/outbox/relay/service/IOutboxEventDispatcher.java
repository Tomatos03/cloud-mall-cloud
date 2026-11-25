package com.cloudmall.outbox.relay.service;

/**
 * Outbox 事件分发口。
 * <p>
 * 由业务服务提供自定义实现，或使用 starter 提供的 RocketMQ 默认实现。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
public interface IOutboxEventDispatcher {

    /**
     * 分发一条 Outbox 事件。
     *
     * @param eventType 事件类型
     * @param bizId 业务 ID
     * @param payload JSON 载荷
     */
    void dispatch(String eventType, String bizId, String payload);
}
