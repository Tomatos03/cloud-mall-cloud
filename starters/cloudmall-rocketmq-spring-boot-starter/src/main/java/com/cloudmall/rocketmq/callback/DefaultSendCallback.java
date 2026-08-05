package com.cloudmall.rocketmq.callback;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;

/**
 * RocketMQ 默认发送回调
 * <p>
 * 记录异步发送结果日志，可通过继承覆写 {@link #onSuccessExt} / {@link #onExceptionExt}
 * 扩展自定义处理逻辑（如重试、告警、持久化）。
 * <p>
 * 使用示例：
 * <pre>{@code
 * rocketMQProducer.sendAsync("order-topic:normal", orderDTO, new DefaultSendCallback("order-topic:normal", orderDTO.getOrderId()));
 * }</pre>
 *
 * @author Tomatos
 */
@Slf4j
@AllArgsConstructor
public class DefaultSendCallback implements SendCallback {

    /** 目标地址（用于日志） */
    private final String destination;

    /** 业务标识（用于日志，如订单号、消息 ID） */
    private final String businessKey;

    /** 消息体简要描述 */
    private final String payloadSummary;

    /**
     * 构造默认回调
     *
     * @param destination 目标地址
     */
    public DefaultSendCallback(String destination) {
        this(destination, null, null);
    }

    /**
     * 构造默认回调
     *
     * @param destination 目标地址
     * @param businessKey 业务标识
     */
    public DefaultSendCallback(String destination, String businessKey) {
        this(destination, businessKey, null);
    }

    @Override
    public void onSuccess(SendResult sendResult) {
        if (businessKey != null) {
            log.info(
                    "RocketMQ async send SUCCESS | destination={} | businessKey={} | msgId={} | queue={}",
                    destination,
                    businessKey,
                    sendResult.getMsgId(),
                    sendResult.getMessageQueue()
            );
        } else {
            log.info(
                    "RocketMQ async send SUCCESS | destination={} | msgId={} | queue={}",
                    destination,
                    sendResult.getMsgId(),
                    sendResult.getMessageQueue()
            );
        }
        onSuccessExt(sendResult);
    }

    @Override
    public void onException(Throwable throwable) {
        if (businessKey != null) {
            log.error(
                    "RocketMQ async send FAILED | destination={} | businessKey={} | payloadSummary={} | error={}",
                    destination,
                    businessKey,
                    payloadSummary,
                    throwable.getMessage(),
                    throwable
            );
        } else {
            log.error(
                    "RocketMQ async send FAILED | destination={} | payloadSummary={} | error={}",
                    destination,
                    payloadSummary,
                    throwable.getMessage(),
                    throwable
            );
        }
        onExceptionExt(throwable);
    }

    /**
     * 发送成功扩展点 <p>
     * 子类可覆写此方法实现额外的成功处理逻辑（如更新数据库状态、发送通知等）。
     *
     * @param sendResult 发送结果
     */
    protected void onSuccessExt(SendResult sendResult) {
        // 子类按需扩展
    }

    /**
     * 发送失败扩展点 <p>
     * 子类可覆写此方法实现额外的失败处理逻辑（如本地消息表回滚、告警通知等）。
     *
     * @param throwable 异常信息
     */
    protected void onExceptionExt(Throwable throwable) {
        // 子类按需扩展
    }
}
