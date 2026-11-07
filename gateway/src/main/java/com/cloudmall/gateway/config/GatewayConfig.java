package com.cloudmall.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.cloudmall.gateway.properties.AuthProperties;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class GatewayConfig {
}
