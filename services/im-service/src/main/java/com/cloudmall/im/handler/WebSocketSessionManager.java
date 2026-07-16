package com.cloudmall.im.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket会话管理器
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@Slf4j
@Component
public class WebSocketSessionManager {
    /**
     * 存储用户ID与WebSocketSession的映射
     */
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    public void addSession(Long userId, WebSocketSession session) {
        userSessions.put(userId, session);
        log.info("用户 {} 已连接 WebSocket", userId);
    }

    public void removeSession(Long userId) {
        userSessions.remove(userId);
        log.info("用户 {} 已断开 WebSocket", userId);
    }

    public boolean isOnline(Long userId, WebSocketSession session) {
        return userSessions.containsKey(userId);
    }

    public WebSocketSession getSession(Long userId) {
        return userSessions.get(userId);
    }
}