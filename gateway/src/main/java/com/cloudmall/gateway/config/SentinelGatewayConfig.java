package com.cloudmall.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudmall.gateway.handler.SentinelGatewayBlockHandler;

/**
 * Sentinel 网关配置
 * <p>
 * 注册自定义 BlockRequestHandler，由 spring-cloud-alibaba-sentinel-gateway
 * 自动配置在启动时写回 GatewayCallbackManager。
 *
 * @author : Tomatos
 */
@Configuration
public class SentinelGatewayConfig {

    /**
     * 网关限流拦截处理器。
     *
     * @return 拦截处理器
     */
    @Bean
    public BlockRequestHandler sentinelBlockRequestHandler() {
        return new SentinelGatewayBlockHandler();
    }
}
