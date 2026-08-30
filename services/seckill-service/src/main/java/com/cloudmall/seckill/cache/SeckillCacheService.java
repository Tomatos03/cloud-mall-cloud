package com.cloudmall.seckill.cache;

import java.util.List;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import com.cloudmall.seckill.enums.SeckillPurchaseResultEnum;

/**
 * 秒杀缓存服务
 * <p>
 * Redis 预热（SET NX 取 DB 初始值）、Lua 原子扣减（一人一单 + 库存校验）、
 * 超时归还（INCR）、清理。
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillCacheService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String STOCK_KEY_PREFIX = "seckill:stock:";
    private static final String USER_FLAG_PREFIX = "seckill:user:";

    /** Lua 原子脚本：一人一单 + 库存扣减（resources/lua/seckill_purchase.lua） */
    private static final RedisScript<Long> LUA_PURCHASE = RedisScript.of(
            new ClassPathResource("lua/seckill_purchase.lua"),
            Long.class
    );

    /**
     * 预热库存 key（SET NX，仅当 key 不存在时写入）。
     *
     * @param activityId 活动 ID
     * @param skuId SKU ID
     * @param stock 秒杀库存
     */
    public void preheatStock(Long activityId, Long skuId, Integer stock) {
        String key = STOCK_KEY_PREFIX + activityId + ":" + skuId;
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, String.valueOf(stock));
        if (Boolean.TRUE.equals(success)) {
            log.info("预热秒杀库存: key={}, stock={}", key, stock);
        }
    }

    /**
     * Lua 原子抢购：一人一单 + 库存扣减。
     *
     * @param activityId 活动 ID
     * @param skuId SKU ID
     * @param userId 用户 ID
     * @param quantity 购买数量（固定 1）
     * @param ttlSeconds 一人一单标记 TTL
     * @return 抢购结果枚举（SUCCESS=成功，REPEAT=已抢购，NO_STOCK=库存不足
     *         ）
     */
    public SeckillPurchaseResultEnum purchase(
        Long activityId,
        Long skuId,
        Long userId,
        int quantity,
        long ttlSeconds
    ) {
        String stockKey = STOCK_KEY_PREFIX + activityId + ":" + skuId;
        String userKey = USER_FLAG_PREFIX + activityId + ":" + skuId + ":" + userId;

        Long code = stringRedisTemplate.execute(
                LUA_PURCHASE,
                List.of(stockKey, userKey),
                String.valueOf(quantity),
                String.valueOf(ttlSeconds)
        );
        return SeckillPurchaseResultEnum.fromCode(
                ObjectUtil.defaultIfNull(code, SeckillPurchaseResultEnum.REPEAT.getCode())
        );
    }

    /**
     * 超时归还库存（INCR）。
     *
     * @param activityId 活动 ID
     * @param skuId SKU ID
     */
    public void refundStock(Long activityId, Long skuId) {
        String key = STOCK_KEY_PREFIX + activityId + ":" + skuId;
        stringRedisTemplate.opsForValue().increment(key);
        log.info("归还秒杀库存: key={}", key);
    }

    /**
     * 抢购回补：归还库存并清除一人一单标记。 <p>
     * 仅用于秒杀单唯一键冲突等落库失败场景，抵消 Lua 已完成的预扣。
     *
     * @param activityId 活动 ID
     * @param skuId SKU ID
     * @param userId 用户 ID
     */
    public void rollback(Long activityId, Long skuId, Long userId) {
        String stockKey = STOCK_KEY_PREFIX + activityId + ":" + skuId;
        String userKey = USER_FLAG_PREFIX + activityId + ":" + skuId + ":" + userId;
        stringRedisTemplate.opsForValue().increment(stockKey);
        stringRedisTemplate.delete(userKey);
        log.info("抢购回补完成 stockKey={} userKey={}", stockKey, userKey);
    }

    /**
     * 删除某个秒杀商品的库存 key（活动结束或取消后由预热任务调用）。 <p>
     * 一人一单标记随 TTL（活动结束时刻）自然过期，无需显式清理。
     *
     * @param activityId 活动 ID
     * @param skuId SKU ID
     */
    public void deleteStock(Long activityId, Long skuId) {
        String key = STOCK_KEY_PREFIX + activityId + ":" + skuId;
        Boolean removed = stringRedisTemplate.delete(key);
        if (removed) {
            log.info("清理秒杀库存缓存: key={}", key);
        }
    }
}
