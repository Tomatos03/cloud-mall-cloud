package com.cloudmall.jwt;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret = "cloud-mall-jwt-secret-key-2026-very-long-secret-key";

    private long expiration = 86400000;
}
