package com.cloudmall.outbox.publisher;

import java.util.Collection;

/**
 * Outbox 事件发布器
 * <p>
 * 分发器面向该抽象发布事件，具体传输（RocketMQ、Feign 等）由实现类提供。
 * 业务服务可通过注册自定义 {@link IOutboxPublisher} Bean 替换默认实现，
 * 分发器与事件注册表不感知传输细节。
 *
 * @author : Tomatos
 */
public interface IOutboxPublisher {

    /**
     * 发布单条事件到指定主题与标签。
     *
     * @param topic 主题
     * @param tag 标签
     * @param event 事件对象
     */
    void publish(String topic, String tag, Object event);

    /**
     * 批量发布事件到指定主题与标签。 <p>
     * 默认逐条回退到 {@link #publish}；支持批量的实现（如 RocketMQ） 应覆盖为真
     * 正的批量发送。
     *
     * @param topic 主题
     * @param tag 标签
     * @param events 事件列表
     */
    default void publishBatch(String topic, String tag, Collection<?> events) {
        events.forEach(event -> publish(topic, tag, event));
    }
}
