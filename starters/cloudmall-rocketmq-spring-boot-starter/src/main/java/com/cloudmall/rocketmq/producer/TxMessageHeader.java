package com.cloudmall.rocketmq.producer;

import org.apache.rocketmq.spring.support.RocketMQHeaders;

/**
 * 事务消息 Header 常量接口
 * <p>
 * 提供事务消息公共 header 常量。事务消息的发送端与
 * {@code RocketMQLocalTransactionListener} 实现类处于同一微服务进程，
 * 统一引用本接口常量，避免 header 名漂移导致回查失配。
 *
 * @author : Tomatos
 */
public interface TxMessageHeader {

    /** 事务 ID（RocketMQ 标准头） */
    String TRANSACTION_ID = RocketMQHeaders.TRANSACTION_ID;

    /** 事件 ID（业务主键，事务监听器回查与消费方幂等共用） */
    String EVENT_ID = "EVENT_ID";

    /** 事件类型（RocketMQ Tag 值） */
    String EVENT_TYPE = "EVENT_TYPE";
}
