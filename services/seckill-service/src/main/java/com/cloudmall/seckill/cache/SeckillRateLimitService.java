package com.cloudmall.seckill.cache;

import java.time.Duration;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;

/**
 * 秒杀限流服务
 * <p>
 * 固定窗口限流（1分钟10次）+ 自动黑名单（超阈值30次/分钟，TTL 24h）。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class SeckillRateLimitService {

    private static final long MAX_PER_MINUTE = 10;
    private static final long BLACKLIST_THRESHOLD = 30;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final Duration BLACKLIST_TTL = Duration.ofHours(24);

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 校验用户抢购频率并记录黑名单。
     *
     * @param userId 用户 ID
     */
    public void checkRate(Long userId) {
        // 黑名单拦截
        Boolean inBlacklist = stringRedisTemplate.hasKey("seckill:blacklist:" + userId);
        Asserts.isFalse(inBlacklist, BizError.SECKILL_RATE_LIMIT_EXCEEDED);

        // 计数
        String key = "seckill:rate:" + userId;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, WINDOW);
        }

        // 超阈值进黑名单
        if (count != null && count >= BLACKLIST_THRESHOLD) {
            stringRedisTemplate.opsForValue().set("seckill:blacklist:" + userId, "1", BLACKLIST_TTL);
        }

        // 超限抛异常
        Asserts.isTrue(count == null || count <= MAX_PER_MINUTE, BizError.SECKILL_RATE_LIMIT_EXCEEDED);
    }
}
