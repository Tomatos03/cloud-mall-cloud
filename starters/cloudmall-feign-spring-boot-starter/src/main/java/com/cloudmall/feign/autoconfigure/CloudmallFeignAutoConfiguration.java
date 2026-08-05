package com.cloudmall.feign.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import com.cloudmall.feign.decoder.ResultUnwrappingDecoder;
import com.cloudmall.feign.interceptor.FeignUserContextInterceptor;

/**
 * Feign 客户端自动配置
 * 自动扫描 com.cloudmall 包下的所有 FeignClient，各服务无需显式声明 @EnableFeignClients
 *
 * @author : Tomatos
 * @date : 2026/7/22
 */
@AutoConfiguration
@EnableFeignClients(basePackages = "com.cloudmall")
public class CloudmallFeignAutoConfiguration {

    /**
     * 创建 Feign 用户上下文拦截器。
     *
     * @param objectMapper JSON 对象映射器
     * @return 处理结果
     */
    @Bean
    FeignUserContextInterceptor feignUserContextInterceptor(ObjectMapper objectMapper) {
        return new FeignUserContextInterceptor(objectMapper);
    }

    /**
     * 创建 Feign Result 自动拆箱解码器。
     *
     * @param objectMapper JSON 对象映射器
     * @return 解码器
     */
    @Bean
    ResultUnwrappingDecoder resultUnwrappingDecoder(ObjectMapper objectMapper) {
        return new ResultUnwrappingDecoder(objectMapper);
    }

}
