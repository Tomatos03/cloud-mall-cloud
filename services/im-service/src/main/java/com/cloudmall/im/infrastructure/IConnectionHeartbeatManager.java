package com.cloudmall.im.infrastructure;

import org.springframework.web.socket.WebSocketSession;

/**
 * 连接心跳管理。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
public interface IConnectionHeartbeatManager {

    void record(WebSocketSession session);

    void refreshPresence(Long userId, String sessionId);

    void ping();

    void cleanupStale();
}
