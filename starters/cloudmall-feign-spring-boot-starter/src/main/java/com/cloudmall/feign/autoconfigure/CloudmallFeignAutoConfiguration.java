package com.cloudmall.feign.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import com.cloudmall.feign.decoder.RemoteErrorDecoder;
import com.cloudmall.feign.decoder.ResultDecoder;
import com.cloudmall.feign.decoder.ResultParser;
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
     * 创建 Result 信封解析器，供解码器与错误解码器共用。
     *
     * @param objectMapper JSON 对象映射器
     * @return 解析器
     */
    @Bean
    ResultParser resultParser(ObjectMapper objectMapper) {
        return new ResultParser(objectMapper);
    }

    /**
     * 创建 Feign Result 自动拆箱解码器。
     *
     * @param parser Result 信封解析器
     * @return 解码器
     */
    @Bean
    ResultDecoder resultDecoder(ResultParser parser) {
        return new ResultDecoder(parser);
    }

    /**
     * 创建远程调用错误解码器，统一翻译非 2xx 响应。
     *
     * @param parser Result 信封解析器
     * @return 错误解码器
     */
    @Bean
    @ConditionalOnMissingBean(ErrorDecoder.class)
    RemoteErrorDecoder remoteErrorDecoder(ResultParser parser) {
        return new RemoteErrorDecoder(parser);
    }

}
