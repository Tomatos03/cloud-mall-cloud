package com.cloudmall.im.infrastructure.websocket.impl;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.cloudmall.im.infrastructure.websocket.IWebSocketSessionRegistry;

/**
 * 本地会话注册表实现。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
@Slf4j
@Component
public class WebSocketSessionRegistry implements IWebSocketSessionRegistry {

    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Long> lastHeartbeat = new ConcurrentHashMap<>();

    @Override
    public void register(Long userId, WebSocketSession session) {
        WebSocketSession previous = sessions.put(userId, session);
        if (previous != null) {
            lastHeartbeat.remove(previous.getId());
        }
        lastHeartbeat.put(session.getId(), System.currentTimeMillis());
    }

    @Override
    public void unregister(Long userId, WebSocketSession session) {
        if (sessions.remove(userId, session)) {
            lastHeartbeat.remove(session.getId());
        }
    }

    @Override
    public Collection<WebSocketSession> find(Long userId) {
        WebSocketSession session = sessions.get(userId);
        return session == null ? List.of() : List.of(session);
    }

    @Override
    public List<Long> findStale(Duration maxIdle) {
        long threshold = System.currentTimeMillis() - maxIdle.toMillis();
        return CollStreamUtil.toList(
                CollUtil.filterNew(
                        sessions.entrySet(),
                        entry -> {
                            Long heartbeat = lastHeartbeat.get(entry.getValue().getId());
                            return heartbeat != null && heartbeat < threshold;
                        }
                ),
                Map.Entry::getKey
        );
    }

    @Override
    public Collection<WebSocketSession> allSessions() {
        return List.copyOf(sessions.values());
    }

    @Override
    public Long findUserId(WebSocketSession session) {
        for (Map.Entry<Long, WebSocketSession> entry : sessions.entrySet()) {
            if (entry.getValue()
                     .getId()
                     .equals(session.getId())) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public void recordHeartbeat(WebSocketSession session) {
        lastHeartbeat.put(session.getId(), System.currentTimeMillis());
    }

    @Override
    public boolean sendToLocal(Long userId, String payload) {
        WebSocketSession session = sessions.get(userId);
        if (session == null || !session.isOpen()) {
            return false;
        }
        try {
            session.sendMessage(new TextMessage(payload));
            return true;
        } catch (IOException e) {
            log.warn("向用户 {} 推送 WebSocket 消息失败，已清理本地会话: {}", userId, e.getMessage());
            unregister(userId, session);
            return false;
        }
    }
}
