package com.cloudmall.im.application.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

import com.cloudmall.im.application.IConnectionHeartbeatManager;
import com.cloudmall.im.infrastructure.store.IPresenceStore;
import com.cloudmall.im.infrastructure.support.IInstanceIdProvider;
import com.cloudmall.im.infrastructure.websocket.IWebSocketSessionRegistry;
import com.cloudmall.im.model.bo.UserRoute;
import com.cloudmall.im.properties.ImProperties;

/**
 * 心跳与半开连接清理实现。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionHeartbeatManager implements IConnectionHeartbeatManager {

    private final IWebSocketSessionRegistry registry;
    private final IPresenceStore presenceStore;
    private final IInstanceIdProvider instanceIdProvider;
    private final ImProperties properties;

    @Override
    public void record(WebSocketSession session) {
        registry.recordHeartbeat(session);
    }

    @Override
    public void refreshPresence(Long userId, String sessionId) {
        presenceStore.refresh(userId, route(sessionId));
    }

    @Override
    public void ping() {
        for (WebSocketSession session : registry.allSessions()) {
            Long userId = registry.findUserId(session);
            if (userId != null) {
                refreshPresence(userId, session.getId());
            }
            if (!(session instanceof StandardWebSocketSession standardSession)) {
                continue;
            }
            jakarta.websocket.Session nativeSession = standardSession.getNativeSession();
            if (nativeSession == null) {
                continue;
            }
            try {
                nativeSession.getBasicRemote().sendPing(ByteBuffer.allocate(0));
            } catch (IOException e) {
                log.debug("向 session {} 发送心跳 ping 失败", session.getId());
            }
        }
    }

    @Override
    public void cleanupStale() {
        Duration maxIdle = Duration.ofSeconds(properties.getHeartbeat().getStaleTimeoutSeconds());
        for (Long userId : registry.findStale(maxIdle)) {
            for (WebSocketSession session : registry.find(userId)) {
                if (session.isOpen()) {
                    try {
                        session.close(CloseStatus.GOING_AWAY);
                    } catch (IOException e) {
                        log.warn("关闭用户 {} 的半开连接失败: {}", userId, e.getMessage());
                    }
                }
                registry.unregister(userId, session);
                presenceStore.unregisterIfMatch(userId, route(session.getId()));
                log.info("用户 {} 心跳超时，已清理半开连接", userId);
            }
        }
    }

    private UserRoute route(String sessionId) {
        return new UserRoute(instanceIdProvider.get(), sessionId, System.currentTimeMillis());
    }
}
