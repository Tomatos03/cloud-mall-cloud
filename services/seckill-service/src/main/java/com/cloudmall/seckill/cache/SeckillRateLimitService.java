package com.cloudmall.seckill.cache;

import java.time.Duration;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;

/**
 * 秒杀限流服务
 * <p>
 * 固定窗口限流（1分钟10次），通过 Lua 脚本原子执行，避免并发竞态。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class SeckillRateLimitService {

    /** 请求计数 key 模板（占位符为 userId） */
    private static final String RATE_LIMIT_KEY_TEMPLATE = "seckill:rate:%d";

    private static final long MAX_PER_MINUTE = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    /** Lua 原子脚本：固定窗口限流（resources/lua/seckill_rate_limit.lua） */
    private static final RedisScript<Long> LUA_RATE_LIMIT = RedisScript.of(
            new ClassPathResource("lua/seckill_rate_limit.lua"),
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 校验用户抢购频率。
     *
     * @param userId 用户 ID
     */
    public void checkRate(Long userId) {
        // Lua 原子限流：key 存在则 INCR，不存在则 SET 1 并设置过期时间
        String rateKey = String.format(RATE_LIMIT_KEY_TEMPLATE, userId);
        Long count = stringRedisTemplate.execute(
                LUA_RATE_LIMIT,
                List.of(rateKey),
                String.valueOf(WINDOW.getSeconds()),
                String.valueOf(MAX_PER_MINUTE)
        );

        // 超限抛异常
        Asserts.isTrue(count == null || count <= MAX_PER_MINUTE, BizErrorEnum.SECKILL_RATE_LIMIT_EXCEEDED);
    }
}
