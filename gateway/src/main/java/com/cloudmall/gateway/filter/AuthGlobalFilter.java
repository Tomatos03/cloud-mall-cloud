package com.cloudmall.gateway.filter;

import com.cloudmall.gateway.config.AuthProperties;
import com.cloudmall.jwt.JwtTokenTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final String AUTH_USER_HEADER = "X-Auth-User";

    private final JwtTokenTemplate jwtTokenTemplate;
    private final ObjectMapper objectMapper;
    private final AuthProperties authProperties;

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
            Claims claims = jwtTokenTemplate.parseSignedClaims(token);

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