package com.cloudmall.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.cloudmall.order.properties.CartCacheProperties;

/**
 * 购物车缓存配置。
 * <p>
 * 使用 Spring Cache 注解 + RedisCacheManager；缓存异常只记录日志并降级回源数据库。
 *
 * @author : Tomatos
 */
@Configuration
@EnableCaching
public class CartCacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CartCacheConfig.class);

    /**
     * 注册购物车缓存配置。
     *
     * @return 购物车缓存配置
     */
    @Bean
    @ConfigurationProperties(prefix = "cloudmall.cart.cache")
    public CartCacheProperties cartCacheProperties() {
        return new CartCacheProperties();
    }

    /**
     * 配置 Redis 缓存管理器：键用字符串序列化，值用 JSON，过期时间由配置控制。
     *
     * @param connectionFactory Redis 连接工厂
     * @param properties 购物车缓存配置
     * @return Redis 缓存管理器
     */
    @Bean
    public RedisCacheManager cartCacheManager(
        RedisConnectionFactory connectionFactory,
        CartCacheProperties properties
    ) {
        ObjectMapper objectMapper = JsonMapper.builder()
                                              .findAndAddModules()
                                              .build();
        RedisCacheConfiguration configuration = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(properties.getTtl())
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> properties.getKeyPrefix() + cacheName + "::")
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()
                        )
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer(objectMapper)
                        )
                );
        return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(configuration)
                                .build();
    }

    /**
     * 缓存异常不向业务层传播，购物车读请求直接回源数据库，写请求继续执行数据库操作。
     *
     * @return 缓存异常处理器
     */
    @Bean
    public CacheErrorHandler cartCacheErrorHandler() {
        return new SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("购物车缓存读取失败，回源数据库 cache={} key={}", cache.getName(), key, exception);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("购物车缓存回填失败 cache={} key={}", cache.getName(), key, exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("购物车缓存失效失败 cache={} key={}", cache.getName(), key, exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("购物车缓存清空失败 cache={}", cache.getName(), exception);
            }
        };
    }
}
