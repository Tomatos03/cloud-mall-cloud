package com.cloudmall.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    // 白名单路径（不需要 token）
    private static final Set<String> WHITE_LIST = Set.of(
            "/auth/sessions", "/auth/users"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 白名单放行
        if (WHITE_LIST.contains(path)) {
            return chain.filter(exchange);
        }

        // 从 Header 获取 token
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 解析 JWT -> 放入 Header 传递给下游
        // 暂简单实现：透传 Token，auth-service 负责校验
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
