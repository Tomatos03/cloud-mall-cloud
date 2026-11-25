package com.cloudmall.im.infrastructure.push.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.im.api.constant.ImEventConstants;
import com.cloudmall.im.api.model.event.ImMessagePushEvent;
import com.cloudmall.im.infrastructure.push.IPushDispatcher;
import com.cloudmall.outbox.relay.service.IOutboxEventDispatcher;

/**
 * 将 Outbox 中的 IM 事件交给推送策略。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
@Component
@RequiredArgsConstructor
public class ImOutboxEventDispatcher implements IOutboxEventDispatcher {

    private final ObjectMapper objectMapper;
    private final IPushDispatcher pushDispatcher;

    @Override
    public void dispatch(String eventType, String bizId, String payload) {
        if (!ImEventConstants.IM_MESSAGE_PUSH.equals(eventType)) {
            return;
        }
        try {
            pushDispatcher.dispatch(objectMapper.readValue(payload, ImMessagePushEvent.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("解析 IM 推送事件失败，bizId=" + bizId, e);
        }
    }
}
