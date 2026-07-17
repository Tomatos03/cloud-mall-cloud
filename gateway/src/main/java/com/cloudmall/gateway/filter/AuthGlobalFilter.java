package com.cloudmall.gateway.filter;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import com.cloudmall.gateway.config.AuthProperties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final String AUTH_USER_HEADER = "X-Auth-User";

    private final SecretKey key;
    private final ObjectMapper objectMapper;
    private final AuthProperties authProperties;

    public AuthGlobalFilter(@Value("${jwt.secret:cloud-mall-jwt-secret-key-2026-very-long-secret-key}") String jwtSecret,
                            ObjectMapper objectMapper,
                            AuthProperties authProperties) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.objectMapper = objectMapper;
        this.authProperties = authProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        String header = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (!hasValidAuthHeader(header)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = header.substring(7);

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = claims.get("userId", Long.class);
            String username = claims.get("username", String.class);
            String userType = claims.get("userType", String.class);

            AuthUserJson authUser = new AuthUserJson(userId, username, userType);
            String authUserJson = objectMapper.writeValueAsString(authUser);

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header(AUTH_USER_HEADER, authUserJson)
                    .build();
            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

            return chain.filter(mutatedExchange);
        } catch (JwtException | JsonProcessingException e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isWhitelisted(String path) {
        return authProperties.getWhitelist().contains(path);
    }

    private boolean hasValidAuthHeader(String header) {
        return header != null && header.startsWith("Bearer ");
    }

    private record AuthUserJson(Long userId, String username, String userType) {}
}