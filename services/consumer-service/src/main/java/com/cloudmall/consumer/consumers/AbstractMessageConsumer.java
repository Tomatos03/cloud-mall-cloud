package com.cloudmall.consumer.consumers;

import java.nio.charset.StandardCharsets;
import java.util.List;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.core.RocketMQListener;

import com.cloudmall.consumer.handler.IEventHandler;

/**
 * 消息消费统一模板
 * <p>
 * 模板统一完成：JSON 解析 → tag 归一 → 注册表分发 → 业务处理 -> 异常处理(子类可覆盖)
 *
 * @param <T> 事件载荷类型
 * @author : Tomatos
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractMessageConsumer<T> implements RocketMQListener<MessageExt> {

    /** 无 tag 消息的注册键 */
    private static final String NO_TAG = "";

    private final List<IEventHandler<T>> eventHandlers;
    private final Class<T> payloadType;

    /**
     * 处理消息：解析、分发、业务处理。
     *
     * @param message 消息
     */
    @Override
    public void onMessage(MessageExt message) {
        T event = parse(message);
        if (event == null) {
            return;
        }
        String tag = normalize(message.getTags());
        for (IEventHandler<T> eventHandler : eventHandlers) {
            if (!eventHandler.canHandle(tag)) {
                continue;
            }
            try {
                eventHandler.handle(event);
            } catch (RuntimeException e) {
                handleException(tag, eventHandler, event, e);
            }
        }
    }

    /**
     * 处理处理器执行异常。 <p>
     * 默认记录失败日志并原样上抛，由 RocketMQ 重试机制兜底； 子类可覆写以自定义处理策略（如降级
     * 、告警， 或吞掉异常继续后续处理器）。
     *
     * @param tag 消息 tag
     * @param eventHandler 发生异常的处理器
     * @param event 事件载荷
     * @param e 处理器抛出的异常
     */
    protected void handleException(String tag, IEventHandler<T> eventHandler, T event, RuntimeException e) {
        log.error("事件处理失败 tag={} eventId={}", tag, eventHandler.eventId(event), e);
        throw e;
    }

    /**
     * 解析消息体为载荷。
     *
     * @param message 消息
     * @return 载荷（解析失败返回 null）
     */
    private T parse(MessageExt message) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            return JSONUtil.toBean(body, payloadType);
        } catch (Exception e) {
            log.error("消息解析失败 msgId={}", message.getMsgId(), e);
            return null;
        }
    }

    /**
     * tag 归一化：null 视为无 tag。
     *
     * @param tag 原始 tag
     * @return 归一化 tag
     */
    private static String normalize(String tag) {
        return tag == null ? NO_TAG : tag;
    }
}
