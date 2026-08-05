package com.cloudmall.rocketmq.consumer;

import java.util.Map;

/**
 * 事件处理器
 * <p>
 * 一个处理器对应一类业务动作，通过 tags() 注册到一个或多个 RocketMQ tag。
 * 幂等由消费模板统一保证：eventId + operationType 落在消费幂等台账。
 *
 * @param <T> 事件载荷类型
 * @author : Tomatos
 */
public interface EventHandler<T> {

    /**
     * 注册的 tag → 幂等操作类型映射（无 tag 的 topic 使用空串）。
     *
     * @return tag 映射
     */
    Map<String, String> tags();

    /**
     * 提取幂等键。
     *
     * @param event 事件
     * @return 事件 ID
     */
    String eventId(T event);

    /**
     * 业务处理（需自身幂等；不抛异常视为处理完成）。
     *
     * @param event 事件
     */
    void handle(T event);
}
