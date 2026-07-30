package com.cloudmall.order.cache;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 购物车缓存服务
 * <p>
 * 封装所有购物车相关的 Redis 操作。
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartCacheService {

    private static final String CART_KEY_PREFIX = "cart:user:";

    private final StringRedisTemplate stringRedisTemplate;

    private String buildKey(String userId) {
        return  CART_KEY_PREFIX + userId;
    }
}
