package com.cloudmall.im.infrastructure.store.impl;

import java.time.Duration;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.cloudmall.im.constant.ImRedisConstants;
import com.cloudmall.im.infrastructure.store.IDeliveryDedupeStore;

/**
 * Redis 推送幂等实现。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
@Component
@RequiredArgsConstructor
public class RedisDeliveryDedupeStore implements IDeliveryDedupeStore {

    private static final Duration TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean tryMark(String messageId) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.opsForValue()
                                   .setIfAbsent(ImRedisConstants.PUSH_DONE_PREFIX + messageId, "1", TTL)
        );
    }
}
