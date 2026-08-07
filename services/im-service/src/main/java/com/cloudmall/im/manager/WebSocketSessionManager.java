package com.cloudmall.im.manager;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket 会话管理器
 * <p>
 * 在线状态存储在 Redis（key: {@code im:online:{userId}}），
 * 实际的 {@link WebSocketSession} 连接对象仍保存在本地 JVM 内存
 * （Redis 无法序列化连接对象）。
 * <p>
 * 多实例部署时：在线状态跨实例可见，但消息投递仍需要连接所在的实例
 * 持有会话对象；当前为单实例部署，本地查询即可命中。
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSessionManager {

    private static final String ONLINE_KEY_PREFIX = "im:online:";
    private static final Duration ONLINE_TTL = Duration.ofMinutes(10);

    /** 用户 ID 与本地连接对象的映射（连接对象不可序列化，只能存内存） */
    private final Map<Long, WebSocketSession> localSessions = new ConcurrentHashMap<>();

    /** 用户 ID 与最近一次活跃（心跳）时间戳的映射，用于检测半开连接 */
    private final Map<Long, Long> lastHeartbeat = new ConcurrentHashMap<>();

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 添加用户连接，并写入 Redis 在线状态。
     *
     * @param userId 用户ID
     * @param session WebSocket 会话
     */
    public void addSession(Long userId, WebSocketSession session) {
        localSessions.put(userId, session);
        lastHeartbeat.put(userId, System.currentTimeMillis());
        stringRedisTemplate.opsForValue().set(onlineKey(userId), session.getId(), ONLINE_TTL);
        log.info("用户 {} 已连接 WebSocket，sessionId: {}，已写入 Redis 在线状态", userId, session.getId());
    }

    /**
     * 移除用户连接，并清除 Redis 在线状态。
     *
     * @param userId 用户ID
     */
    public void removeSession(Long userId) {
        localSessions.remove(userId);
        lastHeartbeat.remove(userId);
        stringRedisTemplate.delete(onlineKey(userId));
        log.info("用户 {} 已断开 WebSocket，已清除 Redis 在线状态", userId);
    }

    /**
     * 用户是否在线（基于 Redis，跨实例可见）。
     *
     * @param userId 用户ID
     * @return true 表示在线
     */
    public boolean isOnline(Long userId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(onlineKey(userId)));
    }

    /**
     * 续期用户在线状态（滑动过期）。 <p>
     * 用户每次活跃（发送消息）时调用，将 Redis 在线状态的 TTL 重新置为
     * {@link #ONLINE_TTL}，保证活跃用户不会被自动判为离线。使用 set 覆盖写入， 即
     * 使 key 已过期，也会重新创建在线状态并续期。
     *
     * @param userId 用户ID
     */
    public void refreshSession(Long userId) {
        WebSocketSession session = localSessions.get(userId);
        if (session != null && session.isOpen()) {
            stringRedisTemplate.opsForValue().set(onlineKey(userId), session.getId(), ONLINE_TTL);
        }
    }

    /**
     * 获取用户在本实例上的 WebSocket 会话。
     *
     * @param userId 用户ID
     * @return WebSocket 会话，本实例不存在时返回 null
     */
    public WebSocketSession getSession(Long userId) {
        return localSessions.get(userId);
    }

    /**
     * 记录用户心跳/活跃时间，用于检测半开连接。
     *
     * @param userId 用户ID
     */
    public void recordHeartbeat(Long userId) {
        lastHeartbeat.put(userId, System.currentTimeMillis());
    }

    /**
     * 找出超过指定时长未心跳的用户（疑似半开连接）。
     *
     * @param maxIdle 允许的最大心跳间隔
     * @return 疑似断连的用户 ID 列表
     */
    public List<Long> findStaleSessionUserIds(Duration maxIdle) {
        long threshold = System.currentTimeMillis() - maxIdle.toMillis();
        return lastHeartbeat.entrySet()
                            .stream()
                            .filter(entry -> entry.getValue() < threshold)
                            .map(Map.Entry::getKey)
                            .toList();
    }

    /**
     * 获取本实例所有在线会话。
     *
     * @return 本地会话集合
     */
    public Collection<WebSocketSession> getAllSessions() {
        return localSessions.values();
    }

    /**
     * 构建在线状态 Redis key。
     *
     * @param userId 用户ID
     * @return Redis key
     */
    private static String onlineKey(Long userId) {
        return ONLINE_KEY_PREFIX + userId;
    }
}
