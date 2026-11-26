package com.cloudmall.order.properties;

import java.time.Duration;

import lombok.Data;

/**
 * 购物车缓存配置。
 *
 * @author : Tomatos
 */
@Data
public class CartCacheProperties {

    /** 是否启用购物车缓存 */
    private boolean enabled = true;

    /** 缓存过期时间 */
    private Duration ttl = Duration.ofMinutes(5);

    /** Spring Cache 键前缀 */
    private String keyPrefix = "";
}
