package com.cloudmall.outbox.relay.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.cloudmall.outbox.relay.properties.OutboxRelayProperties;
import com.cloudmall.outbox.repository.OutboxRepository;

/**
 * 扫描 Outbox 并分发待投递事件。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
@Slf4j
@RequiredArgsConstructor
public class OutboxRelay {

    private final OutboxRepository outboxRepository;
    private final IOutboxEventDispatcher dispatcher;
    private final OutboxRelayProperties properties;

    public void relay() {
        if (
                !properties.isEnabled() || StrUtil.isBlank(properties.getAppName())
                    || CollUtil.isEmpty(properties.getEventTypes())
        ) {
            return;
        }
        outboxRepository.consume(
                properties.getAppName(),
                properties.getEventTypes(),
                record -> dispatcher.dispatch(record.eventType(), record.bizId(), record.payload())
        );
    }
}
