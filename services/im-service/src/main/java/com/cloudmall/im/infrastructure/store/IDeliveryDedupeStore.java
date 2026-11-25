package com.cloudmall.im.infrastructure.store;

/**
 * 推送消费幂等存储。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
public interface IDeliveryDedupeStore {
    boolean tryMark(String messageId);
}
