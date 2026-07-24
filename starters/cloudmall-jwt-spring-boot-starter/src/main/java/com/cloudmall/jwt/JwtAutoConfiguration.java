package com.cloudmall.jwt;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.cloudmall.auth.AuthTokenService;
import com.cloudmall.jwt.token.JwtTokenService;

@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AuthTokenService authTokenService(JwtProperties properties) {
        return new JwtTokenService(properties);
    }
}
