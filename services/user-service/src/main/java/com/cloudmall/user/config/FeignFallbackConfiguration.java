package com.cloudmall.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudmall.product.api.client.fallback.BannerClientFallbackFactory;
import com.cloudmall.product.api.client.fallback.HomeCategoryClientFallbackFactory;
import com.cloudmall.product.api.client.fallback.HomeProductClientFallbackFactory;
import com.cloudmall.seckill.api.client.fallback.SeckillClientFallbackFactory;

/**
 * Feign 兜底回调配置
 * <p>
 * Sentinel Feign 集成要求 fallbackFactory 以 Spring bean 形式存在
 * （{@code FeignClientFactory.getInstance} 按类型从容器查找）。
 * 兜底工厂类位于 service-api 模块（与 Feign 接口同模块），不受服务组件扫描覆盖，
 * 故在此显式注册为 bean。
 *
 * @author : Tomatos
 */
@Configuration
public class FeignFallbackConfiguration {

    /**
     * 注册轮播图客户端兜底工厂。
     *
     * @return 兜底工厂
     */
    @Bean
    public BannerClientFallbackFactory bannerClientFallbackFactory() {
        return new BannerClientFallbackFactory();
    }

    /**
     * 注册首页分类客户端兜底工厂。
     *
     * @return 兜底工厂
     */
    @Bean
    public HomeCategoryClientFallbackFactory homeCategoryClientFallbackFactory() {
        return new HomeCategoryClientFallbackFactory();
    }

    /**
     * 注册首页商品客户端兜底工厂。
     *
     * @return 兜底工厂
     */
    @Bean
    public HomeProductClientFallbackFactory homeProductClientFallbackFactory() {
        return new HomeProductClientFallbackFactory();
    }

    /**
     * 注册秒杀客户端兜底工厂。
     *
     * @return 兜底工厂
     */
    @Bean
    public SeckillClientFallbackFactory seckillClientFallbackFactory() {
        return new SeckillClientFallbackFactory();
    }
}
