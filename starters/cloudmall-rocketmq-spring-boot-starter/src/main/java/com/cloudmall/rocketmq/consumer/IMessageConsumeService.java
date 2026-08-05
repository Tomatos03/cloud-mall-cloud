package com.cloudmall.rocketmq.consumer;

/**
 * 消息消费幂等服务
 * <p>
 * 基于消费幂等台账唯一键保证同一事件同一操作只消费一次，
 * 支持安全重放：业务幂等 + 消费记录兜底。
 * 实现由应用侧提供（消费方服务），consumer 标识与 operationType 词汇由实现方决定。
 *
 * @author : Tomatos
 */
public interface IMessageConsumeService {

    /**
     * 判断事件是否已消费。
     *
     * @param eventId 事件 ID
     * @param operationType 操作类型（应用侧定义，如 ORDER_PAID）
     * @return true 表示已消费
     */
    boolean alreadyConsumed(String eventId, String operationType);

    /**
     * 标记事件消费成功（唯一键冲突忽略，表示并发已消费）。
     *
     * @param eventId 事件 ID
     * @param operationType 操作类型（应用侧定义，如 ORDER_PAID）
     */
    void markConsumed(String eventId, String operationType);
}
