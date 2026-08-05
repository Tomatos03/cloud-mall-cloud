package com.cloudmall.outbox.publisher;

import java.util.Collection;

import lombok.RequiredArgsConstructor;

import com.cloudmall.rocketmq.producer.MQProducer;

/**
 * 基于 RocketMQ 的 Outbox 事件发布器
 * <p>
 * 默认传输实现，通过 {@link MQProducer} 发送事件到 RocketMQ。
 * 批量发送时整批一次网络请求（同 topic 约束由分发器分组保证）。
 *
 * @author : Tomatos
 */
@RequiredArgsConstructor
public class RocketMqOutboxPublisher implements OutboxPublisher {

    private final MQProducer mqProducer;

    @Override
    public void publish(String topic, String tag, Object event) {
        mqProducer.sendSync(topic, tag, event);
    }

    @Override
    public void publishBatch(String topic, String tag, Collection<?> events) {
        mqProducer.sendSyncBatch(topic, tag, events);
    }
}
