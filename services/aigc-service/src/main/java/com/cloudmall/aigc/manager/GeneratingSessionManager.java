package com.cloudmall.aigc.manager;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 生成中会话管理器
 * 基于 Redis 实现分布式锁，支持多实例下流式输出的打断
 *
 * @author : Tomatos
 * @date : 2026/7/22
 */
@Component
@RequiredArgsConstructor
public class GeneratingSessionManager {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "session:generating:";
    private static final Duration TTL = Duration.ofMinutes(3);

    /**
     * 标记会话开始生成
     *
     * @param sessionId 会话 ID
     */
    public void start(String sessionId) {
        stringRedisTemplate.opsForValue().set(buildKey(sessionId), "1", TTL);
    }

    /**
     * 外部主动停止生成
     *
     * @param sessionId 会话 ID
     */
    public void stop(String sessionId) {
        stringRedisTemplate.delete(buildKey(sessionId));
    }

    /**
     * 生成结束清理（正常完成/异常/取消）
     *
     * @param sessionId 会话 ID
     */
    public void finish(String sessionId) {
        stringRedisTemplate.delete(buildKey(sessionId));
    }

    /**
     * 检查会话是否仍在生成中
     *
     * @param sessionId 会话 ID
     * @return true 表示正在生成，false 表示未在生成
     */
    public boolean isGenerating(String sessionId) {
        return stringRedisTemplate.hasKey(buildKey(sessionId));
    }

    /**
     * 构建 Redis key
     *
     * @param sessionId 会话 ID
     * @return 完整的 Redis key
     */
    private String buildKey(String sessionId) {
        return KEY_PREFIX + sessionId;
    }
}
