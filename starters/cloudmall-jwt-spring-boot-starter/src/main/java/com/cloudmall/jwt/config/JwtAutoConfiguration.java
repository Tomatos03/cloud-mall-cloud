package com.cloudmall.jwt.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.cloudmall.jwt.service.ITokenService;
import com.cloudmall.jwt.service.JwtTokenService;

@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ITokenService authTokenService(JwtProperties properties) {
        return new JwtTokenService(properties);
    }
}
