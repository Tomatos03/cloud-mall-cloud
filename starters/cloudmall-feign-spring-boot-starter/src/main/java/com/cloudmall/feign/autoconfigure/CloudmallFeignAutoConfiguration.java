package com.cloudmall.feign.autoconfigure;

import com.cloudmall.feign.interceptor.FeignUserContextInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 客户端自动配置
 * 自动扫描 com.cloudmall 包下的所有 FeignClient，各服务无需显式声明 @EnableFeignClients
 *
 * @author : Tomatos
 * @date : 2026/7/22
 */
@Configuration
@EnableFeignClients(basePackages = "com.cloudmall")
public class CloudmallFeignAutoConfiguration {

    @Bean
    FeignUserContextInterceptor feignUserContextInterceptor(ObjectMapper objectMapper) {
        return new FeignUserContextInterceptor(objectMapper);
    }
}
