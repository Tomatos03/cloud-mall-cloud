package com.cloudmall.gateway.config;

import java.util.List;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    private List<String> whitelist = List.of("/auth/sessions", "/auth/users");
}
