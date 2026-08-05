package com.cloudmall.rocketmq.producer;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.rocketmq.callback.DefaultSendCallback;

/**
 * RocketMQ 通用消息发送客户端
 * <p>
 * 对 {@link RocketMQTemplate} 的轻量封装，提供同步发送、异步发送、单向发送、
 * 延迟消息、顺序消息和事务消息等常用发送方式。
 * <p>
 * <strong>目标地址格式：</strong>{@code topicName:tags}，例如 {@code order-topic:normal}。
 * 当不指定 tag 时，冒号可省略。
 *
 * @author Tomatos
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class MQProducer {

    /**
     * -- GETTER --
     *  获取原生 RocketMQTemplate
     *
     * @return RocketMQTemplate 实例
     */
    private final RocketMQTemplate rocketMQTemplate;

    /**
     * 同步发送消息
     *
     * @param destination 目标地址，格式 {@code topic:tag}
     * @param payload 消息体
     * @param <T> 消息体类型
     * @return 发送结果
     */
    public <T> SendResult sendSync(String destination, T payload) {
        return rocketMQTemplate.syncSend(destination, payload);
    }

    /**
     * 同步发送消息，超时控制
     *
     * @param destination 目标地址，格式 {@code topic:tag}
     * @param payload 消息体
     * @param timeout 发送超时（毫秒）
     * @param <T> 消息体类型
     * @return 发送结果
     */
    public <T> SendResult sendSync(String destination, T payload, long timeout) {
        return rocketMQTemplate.syncSend(destination, payload, timeout);
    }

    /**
     * 同步发送消息，指定 topic 和 tag
     *
     * @param topic 主题
     * @param tag 标签
     * @param payload 消息体
     * @param <T> 消息体类型
     * @return 发送结果
     */
    public <T> SendResult sendSync(String topic, String tag, T payload) {
        return sendSync(buildDestination(topic, tag), payload);
    }

    /**
     * 同步发送消息，指定 topic、tag 和消息 key
     *
     * @param topic 主题
     * @param tag 标签
     * @param payload 消息体
     * @param keys 消息 key（用于去重和追溯）
     * @param <T> 消息体类型
     * @return 发送结果
     */
    public <T> SendResult sendSync(String topic, String tag, T payload, String keys) {
        Message<T> message = MessageBuilder.withPayload(payload)
                                           .setHeader("KEYS", keys)
                                           .build();
        return rocketMQTemplate.syncSend(buildDestination(topic, tag), message);
    }

    /**
     * 同步批量发送消息，指定 topic 和 tag。 <p>
     * 整批一次网络请求，所有消息发往同一 destination（topic:tag）； 发送是原子的，成
     * 功即整批进入 broker。
     *
     * @param topic 主题
     * @param tag 标签
     * @param payloads 消息体列表
     * @param <T> 消息体类型
     * @return 发送结果
     */
    public <T> SendResult sendSyncBatch(String topic, String tag, Collection<T> payloads) {
        List<Message<T>> messages = payloads.stream()
                                            .map(payload -> MessageBuilder.withPayload(payload).build())
                                            .toList();
        return rocketMQTemplate.syncSend(buildDestination(topic, tag), messages);
    }

    /**
     * 同步发送顺序消息
     *
     * @param destination 目标地址，格式 {@code topic:tag}
     * @param payload 消息体
     * @param hashKey 用于选择队列的哈希键（如 orderId）
     * @param <T> 消息体类型
     * @return 发送结果
     */
    public <T> SendResult sendSyncOrderly(String destination, T payload, String hashKey) {
        return rocketMQTemplate.syncSendOrderly(destination, payload, hashKey);
    }

    /**
     * 异步发送消息
     *
     * @param destination 目标地址，格式 {@code topic:tag}
     * @param payload 消息体
     * @param sendCallback 发送回调
     * @param <T> 消息体类型
     */
    public <T> void sendAsync(String destination, T payload, SendCallback sendCallback) {
        rocketMQTemplate.asyncSend(destination, payload, sendCallback);
    }

    /**
     * 异步发送消息，超时控制
     *
     * @param destination 目标地址，格式 {@code topic:tag}
     * @param payload 消息体
     * @param sendCallback 发送回调
     * @param timeout 发送超时（毫秒）
     * @param <T> 消息体类型
     */
    public <T> void sendAsync(String destination, T payload, SendCallback sendCallback, long timeout) {
        rocketMQTemplate.asyncSend(destination, payload, sendCallback, timeout);
    }

    /**
     * 异步发送消息，指定 topic 和 tag
     *
     * @param topic 主题
     * @param tag 标签
     * @param payload 消息体
     * @param sendCallback 发送回调
     * @param <T> 消息体类型
     */
    public <T> void sendAsync(String topic, String tag, T payload, SendCallback sendCallback) {
        sendAsync(buildDestination(topic, tag), payload, sendCallback);
    }

    /**
     * 异步发送消息，使用默认回调（自动记录成功/失败日志）
     *
     * @param destination 目标地址，格式 {@code topic:tag}
     * @param payload 消息体
     * @param <T> 消息体类型
     */
    public <T> void sendAsync(String destination, T payload) {
        sendAsync(destination, payload, new DefaultSendCallback(destination));
    }

    /**
     * 异步发送消息，使用默认回调（携带业务 key）
     *
     * @param destination 目标地址，格式 {@code topic:tag}
     * @param payload 消息体
     * @param businessKey 业务标识（如订单号）
     * @param <T> 消息体类型
     */
    public <T> void sendAsync(String destination, T payload, String businessKey) {
        String summary = safeSummary(payload);
        sendAsync(destination, payload, new DefaultSendCallback(destination, businessKey, summary));
    }

    /**
     * 异步发送消息，通过回调工厂函数按需创建回调实例
     *
     * @param destination 目标地址，格式 {@code topic:tag}
     * @param payload 消息体
     * @param callbackFactory 回调工厂（参数为 destination）
     * @param <T> 消息体类型
     */
    public <T> void sendAsync(String destination, T payload, Function<String, SendCallback> callbackFactory) {
        sendAsync(destination, payload, callbackFactory.apply(destination));
    }

    /**
     * 异步发送消息，指定 topic 和 tag，使用默认回调
     *
     * @param topic 主题
     * @param tag 标签
     * @param payload 消息体
     * @param <T> 消息体类型
     */
    public <T> void sendAsync(String topic, String tag, T payload) {
        String destination = buildDestination(topic, tag);
        sendAsync(destination, payload, new DefaultSendCallback(destination));
    }

    /**
     * 单向发送消息（不等待 Broker 确认）
     *
     * @param destination 目标地址，格式 {@code topic:tag}
     * @param payload 消息体
     * @param <T> 消息体类型
     */
    public <T> void sendOneWay(String destination, T payload) {
        rocketMQTemplate.sendOneWay(destination, payload);
    }

    /**
     * 单向发送消息，指定 topic 和 tag
     *
     * @param topic 主题
     * @param tag 标签
     * @param payload 消息体
     * @param <T> 消息体类型
     */
    public <T> void sendOneWay(String topic, String tag, T payload) {
        sendOneWay(buildDestination(topic, tag), payload);
    }

    /**
     * 同步发送延迟消息（使用延迟级别） <p>
     * RocketMQ 延迟级别（1~18）对应延迟时间： 1s, 5s, 10s, 30s, 1m,
     * 2m, 3m, 4m, 5m, 6m, 7m, 8m, 9m, 10m, 20m, 30m, 1h,
     * 2h
     *
     * @param destination 目标地址，格式 {@code topic:tag}
     * @param payload 消息体
     * @param delayLevel 延迟级别（0 表示不延迟）
     * @param <T> 消息体类型
     * @return 发送结果
     */
    public <T> SendResult sendDelay(String destination, T payload, int delayLevel) {
        Message<T> message = MessageBuilder.withPayload(payload).build();
        return rocketMQTemplate.syncSend(
                destination,
                message,
                rocketMQTemplate.getProducer().getSendMsgTimeout(),
                delayLevel
        );
    }

    /**
     * 同步发送延迟消息，指定 topic 和 tag
     *
     * @param topic 主题
     * @param tag 标签
     * @param payload 消息体
     * @param delayLevel 延迟级别
     * @param <T> 消息体类型
     * @return 发送结果
     */
    public <T> SendResult sendDelay(String topic, String tag, T payload, int delayLevel) {
        return sendDelay(buildDestination(topic, tag), payload, delayLevel);
    }

    /**
     * 同步发送延迟消息（按秒延迟，需 Broker v5.0+ 支持）
     *
     * @param destination 目标地址，格式 {@code topic:tag}
     * @param payload 消息体
     * @param delayTime 延迟时间（秒）
     * @param <T> 消息体类型
     * @return 发送结果
     */
    public <T> SendResult sendDelaySeconds(String destination, T payload, long delayTime) {
        return rocketMQTemplate.syncSendDelayTimeSeconds(destination, payload, delayTime);
    }

    /**
     * 发送事务消息 <p>
     * 使用前需通过 {@code @ExtRocketMQTemplateConfiguration} 配
     * 置事务监听器。
     *
     * @param destination 目标地址，格式 {@code topic:tag}
     * @param payload 消息体
     * @param arg 事务回调参数
     * @param <T> 消息体类型
     * @return 事务发送结果
     */
    public <T> TransactionSendResult sendInTransaction(String destination, T payload, Object arg) {
        Message<T> message = MessageBuilder.withPayload(payload).build();
        return rocketMQTemplate.sendMessageInTransaction(destination, message, arg);
    }

    /**
     * 同步批量发送消息
     *
     * @param destination 目标地址，格式 {@code topic:tag}
     * @param payloads 消息体集合
     * @param <T> 消息体类型
     * @return 发送结果
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> SendResult sendBatch(String destination, Collection<T> payloads) {
        Asserts.notEmpty(payloads, BizError.INVALID_PARAM);
        Collection<Message> messages = payloads.stream()
                                               .map(p -> (Message) MessageBuilder.withPayload(p).build())
                                               .collect(Collectors.toList());
        return rocketMQTemplate.syncSend(destination, messages);
    }

    /**
     * 同步批量发送消息，超时控制
     *
     * @param destination 目标地址，格式 {@code topic:tag}
     * @param payloads 消息体集合
     * @param timeout 发送超时（毫秒）
     * @param <T> 消息体类型
     * @return 发送结果
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> SendResult sendBatch(String destination, Collection<T> payloads, long timeout) {
        Asserts.notEmpty(payloads, BizError.INVALID_PARAM);
        Collection<Message> messages = payloads.stream()
                                               .map(p -> (Message) MessageBuilder.withPayload(p).build())
                                               .collect(Collectors.toList());
        return rocketMQTemplate.syncSend(destination, messages, timeout);
    }

    /**
     * 同步发送预构建的 Spring Message
     *
     * @param destination 目标地址，格式 {@code topic:tag}
     * @param message Spring Message
     * @return 发送结果
     */
    public SendResult sendMessage(String destination, Message<?> message) {
        return rocketMQTemplate.syncSend(destination, message);
    }

    /**
     * 构建 RocketMQ 目标地址
     *
     * @param topic 主题
     * @param tag 标签（为空时不追加）
     * @return 目标地址字符串
     */
    private static String buildDestination(String topic, String tag) {
        if (StrUtil.isBlank(tag)) {
            return topic;
        }
        return topic + ":" + tag;
    }

    /**
     * 安全获取消息体简述（用于日志）
     */
    private static String safeSummary(Object payload) {
        if (payload == null) {
            return "null";
        }
        String str = payload.toString();
        return str.length() > 80 ? str.substring(0, 80) + "..." : str;
    }
}
