package com.cloudmall.im.manager;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
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

    /** 仅当在线标记仍属于该会话时才删除，避免误删重连后的新会话（lua/delete_online_if_match.lua） */
    private static final RedisScript<Long> DELETE_ONLINE_IF_MATCH = RedisScript.of(
            new ClassPathResource("lua/delete_online_if_match.lua"),
            Long.class
    );

    /** 用户 ID 与本地连接对象的映射（连接对象不可序列化，只能存内存） */
    private final Map<Long, WebSocketSession> localSessions = new ConcurrentHashMap<>();

    /** sessionId 与最近一次活跃（心跳）时间戳的映射，按会话隔离，旧会话清理不会碰到新会话 */
    private final Map<String, Long> lastHeartbeat = new ConcurrentHashMap<>();

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 添加用户连接，并写入 Redis 在线状态。
     *
     * @param userId 用户ID
     * @param session WebSocket 会话
     */
    public void addSession(Long userId, WebSocketSession session) {
        localSessions.put(userId, session);
        lastHeartbeat.put(session.getId(), System.currentTimeMillis());
        stringRedisTemplate.opsForValue().set(onlineKey(userId), session.getId(), ONLINE_TTL);
        log.info("用户 {} 已连接 WebSocket，sessionId: {}，已写入 Redis 在线状态", userId, session.getId());
    }

    /**
     * 移除用户连接，并清除 Redis 在线状态。 <p>
     * 仅当 userId 当前对应的仍是传入会话时才清理：旧连接延迟到达的关闭回调 不会误删用户重连后的新
     * 会话。
     *
     * @param userId 用户ID
     * @param session 触发关闭的会话
     */
    public void removeSession(Long userId, WebSocketSession session) {
        if (!localSessions.remove(userId, session)) {
            log.debug("用户 {} 的连接已变更，跳过旧会话 {} 的清理", userId, session.getId());
            return;
        }
        lastHeartbeat.remove(session.getId());
        stringRedisTemplate.execute(DELETE_ONLINE_IF_MATCH, List.of(onlineKey(userId)), session.getId());
        log.info("用户 {} 已断开 WebSocket，sessionId: {}，已清除 Redis 在线状态", userId, session.getId());
    }

    /**
     * 清理本地已无连接的用户在线状态。 <p>
     * 用于本地会话表已不存在该用户、但 Redis 仍残留在线标记的场景。
     *
     * @param userId 用户ID
     */
    public void removeStaleOnlineState(Long userId) {
        if (localSessions.computeIfPresent(userId, (key, current) -> current) != null) {
            return;
        }
        // 清除已无对应本地会话的残留心跳，避免长期积累
        Set<String> activeSessionIds = CollStreamUtil.toSet(localSessions.values(), WebSocketSession::getId);
        lastHeartbeat.keySet().removeIf(sessionId -> !activeSessionIds.contains(sessionId));
        stringRedisTemplate.delete(onlineKey(userId));
        log.info("用户 {} 无本地连接，已清理 Redis 在线状态", userId);
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
     * @param session 当前活跃的会话
     */
    public void recordHeartbeat(WebSocketSession session) {
        lastHeartbeat.put(session.getId(), System.currentTimeMillis());
    }

    /**
     * 找出超过指定时长未心跳的用户（疑似半开连接）。
     *
     * @param maxIdle 允许的最大心跳间隔
     * @return 疑似断连的用户 ID 列表
     */
    public List<Long> findStaleSessionUserIds(Duration maxIdle) {
        long threshold = System.currentTimeMillis() - maxIdle.toMillis();
        return CollStreamUtil.toList(
                CollUtil.filterNew(
                        localSessions.entrySet(),
                        entry -> {
                            Long heartbeat = lastHeartbeat.get(entry.getValue().getId());
                            return heartbeat != null && heartbeat < threshold;
                        }
                ),
                Map.Entry::getKey
        );
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
