package com.cloudmall.outbox.relay.service;

import java.util.Map;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.cloudmall.outbox.relay.properties.OutboxRelayProperties;
import com.cloudmall.rocketmq.producer.MQProducer;

/**
 * 默认 Outbox 事件分发器：按配置映射到 RocketMQ topic/tag。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
@Slf4j
@RequiredArgsConstructor
public class RocketMQOutboxEventDispatcher implements IOutboxEventDispatcher {

    private final MQProducer mqProducer;
    private final ObjectMapper objectMapper;
    private final OutboxRelayProperties properties;

    @Override
    public void dispatch(String eventType, String bizId, String payload) {
        OutboxRelayProperties.Mapping mapping = properties.getMappings().get(eventType);
        if (mapping == null || StrUtil.isBlank(mapping.getTopic())) {
            throw new IllegalStateException("未配置 Outbox 事件的 MQ 映射: " + eventType);
        }
        Map<String, Object> body = parse(payload);
        String keys = bizId;
        String hashKey = resolveHashKey(mapping, body, keys);
        if (mapping.isOrderly()) {
            mqProducer.sendSyncOrderly(destination(mapping), body, hashKey, keys);
        } else {
            mqProducer.sendSync(mapping.getTopic(), mapping.getTag(), body, keys);
        }
        log.info(
                "Outbox 事件已投递 MQ，eventType={} bizId={} destination={}",
                eventType,
                bizId,
                destination(mapping)
        );
    }

    private Map<String, Object> parse(String payload) {
        try {
            return objectMapper.readValue(
                    payload,
                    new TypeReference<>() {
                    }
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Outbox 事件载荷不是合法 JSON", e);
        }
    }

    private static String resolveHashKey(
        OutboxRelayProperties.Mapping mapping,
        Map<String, Object> body,
        String fallback
    ) {
        if (!mapping.isOrderly() || StrUtil.isBlank(mapping.getHashKeyField())) {
            return fallback;
        }
        Object value = body.get(mapping.getHashKeyField());
        return value == null ? fallback : String.valueOf(value);
    }

    private static String destination(OutboxRelayProperties.Mapping mapping) {
        return StrUtil.isBlank(mapping.getTag())
                ? mapping.getTopic()
                : mapping.getTopic() + ":" + mapping.getTag();
    }
}
