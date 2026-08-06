package com.cloudmall.rocketmq.handler;

import java.util.Set;

/**
 * 事件处理器
 * <p>
 * 一个处理器对应一类业务动作，通过 {@link #tagSet()} 声明服务的 RocketMQ tag
 * 集合，由 {@link #canHandle(String)} 按消息携带的 tag 判定是否处理。
 *
 * @param <T> 事件载荷类型
 * @author : Tomatos
 */
public interface EventHandler<T> {

    /**
     * 本处理器服务的 RocketMQ tag 集合（无 tag 的 topic 使用空串）。
     *
     * @return tag 集合
     */
    Set<String> tagSet();

    /**
     * 判断事件是否由本处理器处理。 <p>
     * 消费模板逐处理器调用，返回 false 时跳过该处理器继续尝试下一个； 依据消息携带的 tag 判定
     * 。
     *
     * @param tag 消息 tag
     * @return true 表示本处理器处理该事件
     */
    boolean canHandle(String tag);
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
