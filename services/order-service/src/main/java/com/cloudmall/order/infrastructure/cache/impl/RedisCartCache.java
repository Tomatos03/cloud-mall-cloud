package com.cloudmall.order.infrastructure.cache.impl;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.cloudmall.order.constant.CartCacheConstants;
import com.cloudmall.order.infrastructure.cache.ICartCache;
import com.cloudmall.order.model.entity.CartDO;

/**
 * 基于 Spring Cache 注解的购物车缓存实现。
 * <p>
 * 注解条件引用 {@code cartCacheProperties}，缓存关闭时所有方法直接走业务代码。
 *
 * @author : Tomatos
 */
@Service
public class RedisCartCache implements ICartCache {

    @Override
    @Cacheable(
            cacheNames = CartCacheConstants.CART_ITEMS,
            key = "#userId",
            condition = "@cartCacheProperties.enabled && #userId != null",
            unless = "#result == null"
    )
    public List<CartDO> getOrLoad(Long userId, Supplier<List<CartDO>> loader) {
        return loader.get();
    }

    @Override
    @CacheEvict(
            cacheNames = CartCacheConstants.CART_ITEMS,
            key = "#userId",
            beforeInvocation = true,
            condition = "@cartCacheProperties.enabled && #userId != null"
    )
    public void evict(Long userId) {}
}
