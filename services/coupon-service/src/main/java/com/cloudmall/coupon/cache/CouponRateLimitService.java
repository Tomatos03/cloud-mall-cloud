package com.cloudmall.coupon.cache;

import java.time.Duration;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;

/**
 * 领取限流服务
 * <p>
 * 基于 Redis 计数器对同一用户领取接口限流，防止恶意刷券。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class CouponRateLimitService {

    /** 每分钟领取次数上限 */
    private static final long MAX_CLAIM_PER_MINUTE = 10;

    /** 限流窗口 */
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 校验当前用户领取频率，超限抛业务异常。
     *
     * @param userId 用户 ID
     */
    public void checkClaimRate(Long userId) {
        String key = "coupon:rate:claim:" + userId;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, WINDOW);
        }
        Asserts.isTrue(count == null || count <= MAX_CLAIM_PER_MINUTE, BizError.COUPON_CLAIM_TOO_FREQUENT);
    }
}
