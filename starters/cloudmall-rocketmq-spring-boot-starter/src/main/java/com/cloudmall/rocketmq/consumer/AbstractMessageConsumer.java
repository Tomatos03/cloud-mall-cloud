package com.cloudmall.rocketmq.consumer;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.core.RocketMQListener;

/**
 * 消息消费统一模板
 * <p>
 * 每个 topic 一个薄子类，仅声明 {@code @RocketMQMessageListener} 并传入载荷类型与处理器集合。
 * 模板统一完成：JSON 解析 → tag 归一 → 注册表分发 → 幂等守卫 → 业务处理 → 记账 → 失败重投。
 * 无 tag 的消息以空串作为注册键。幂等台账由应用侧实现 {@link IMessageConsumeService} 提供。
 *
 * @param <T> 事件载荷类型
 * @author : Tomatos
 */
@Slf4j
public abstract class AbstractMessageConsumer<T> implements RocketMQListener<MessageExt> {

    /** 无 tag 消息的注册键 */
    private static final String NO_TAG = "";

    private final IMessageConsumeService consumeService;
    private final Class<T> payloadType;
    private final Map<String, Registration<T>> registry;

    /**
     * 构造器。
     *
     * @param consumeService 消费幂等服务
     * @param handlers 事件处理器
     * @param payloadType 载荷类型
     */
    protected AbstractMessageConsumer(
            IMessageConsumeService consumeService,
            List<EventHandler<T>> handlers,
            Class<T> payloadType
    ) {
        this.consumeService = consumeService;
        this.payloadType = payloadType;
        this.registry = buildRegistry(handlers);
    }

    /**
     * 处理消息：解析、分发、幂等、记账。
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
        Registration<T> registration = registry.get(tag);
        if (registration == null) {
            log.warn("未注册的 tag={}，跳过 msgId={}", tag, message.getMsgId());
            return;
        }
        String eventId = registration.handler.eventId(event);
        if (StrUtil.isBlank(eventId)) {
            return;
        }
        if (consumeService.alreadyConsumed(eventId, registration.operationType)) {
            return;
        }
        try {
            registration.handler.handle(event);
            consumeService.markConsumed(eventId, registration.operationType);
        } catch (RuntimeException e) {
            log.error("事件处理失败 tag={} eventId={}", tag, eventId, e);
            throw e;
        }
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

    /**
     * 构建注册表，重复 tag 快速失败。
     *
     * @param handlers 处理器列表
     * @param <T> 载荷类型
     * @return tag → 注册项
     */
    private static <T> Map<String, Registration<T>> buildRegistry(List<EventHandler<T>> handlers) {
        Map<String, Registration<T>> registry = new LinkedHashMap<>();
        if (CollUtil.isEmpty(handlers)) {
            log.warn("未注入任何 EventHandler，请检查处理器泛型声明");
            return registry;
        }
        for (EventHandler<T> handler : handlers) {
            for (Map.Entry<String, String> entry : handler.tags().entrySet()) {
                Registration<T> previous = registry.put(
                        entry.getKey(),
                        new Registration<>(handler, entry.getValue())
                );
                if (previous != null) {
                    throw new IllegalStateException("重复注册 tag=" + entry.getKey());
                }
            }
        }
        return registry;
    }

    /**
     * 注册项：处理器 + 幂等操作类型。
     *
     * @param handler 处理器
     * @param operationType 幂等操作类型
     */
    private record Registration <T>(EventHandler<T> handler, String operationType) {
    }
}
