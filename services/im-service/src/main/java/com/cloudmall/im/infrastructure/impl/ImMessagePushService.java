package com.cloudmall.im.infrastructure.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.cloudmall.im.api.model.resp.MessageResp;
import com.cloudmall.im.infrastructure.IImMessagePushService;
import com.cloudmall.im.infrastructure.IWebSocketSessionRegistry;

/**
 * 本地 WebSocket 推送实现。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImMessagePushService implements IImMessagePushService {

    private final IWebSocketSessionRegistry registry;
    private final ObjectMapper objectMapper;

    @Override
    public boolean pushToLocal(Long receiverId, MessageResp message) {
        try {
            return registry.sendToLocal(receiverId, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            log.error("序列化推送消息失败，receiverId={}", receiverId, e);
            return false;
        }
    }
}
