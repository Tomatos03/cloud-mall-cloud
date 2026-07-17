package com.cloudmall.jwt;

import com.cloudmall.jwt.token.JwtTokenTemplate;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration {

    @Bean
    public JwtTokenTemplate jwtTokenTemplate(JwtProperties properties) {
        return new JwtTokenTemplate(properties);
    }
}
