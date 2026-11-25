package com.cloudmall.im.infrastructure;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

import org.springframework.web.socket.WebSocketSession;

/**
 * 本地 WebSocket 会话注册表。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
public interface IWebSocketSessionRegistry {

    void register(Long userId, WebSocketSession session);

    void unregister(Long userId, WebSocketSession session);

    Collection<WebSocketSession> find(Long userId);

    List<Long> findStale(Duration maxIdle);

    Collection<WebSocketSession> allSessions();

    Long findUserId(WebSocketSession session);

    void recordHeartbeat(WebSocketSession session);

    boolean sendToLocal(Long userId, String payload);
}
