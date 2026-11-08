package com.cloudmall.jwt.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.cloudmall.jwt.properties.JwtProperties;
import com.cloudmall.jwt.service.ITokenService;
import com.cloudmall.jwt.service.JwtTokenService;

@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration {

    /**
     * 创建令牌服务。
     *
     * @param properties 配置属性
     * @return 处理结果
     */
    @Bean
    @ConditionalOnMissingBean
    public ITokenService jwtTokenService(JwtProperties properties) {
        return new JwtTokenService(properties);
    }
}
