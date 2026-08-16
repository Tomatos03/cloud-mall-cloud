package com.cloudmall.gateway.filter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.cloudmall.common.context.UserContext;
import com.cloudmall.common.model.Result;
import com.cloudmall.jwt.exception.AuthTokenException;
import com.cloudmall.jwt.service.ITokenService;

/**
 * JWT 认证过滤器
 * <p>
 * 替代原有的 AuthGlobalFilter，以 Spring Security WebFilter 形式接入安全链。
 * 解析 JWT Token 后构建 Authentication 并写入 ReactiveSecurityContextHolder，
 * 供 RbacReactiveAuthorizationManager 获取当前用户的权限信息。
 * <p>
 * 白名单路径由 GatewaySecurityConfig 的 pathMatchers().permitAll() 管理，
 * 本过滤器不再处理白名单逻辑。
 * <p>
 * 处理流程：
 * <ul>
 *     <li>无 Authorization 头 → 匿名放行（授权管理器决定是否拒绝）</li>
 *     <li>有有效 Token → 解析认证信息，写入 SecurityContext</li>
 *     <li>Token 无效 → 返回 401</li>
 * </ul>
 *
 * @author Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationWebFilter implements WebFilter {

    private static final String AUTH_USER_HEADER = "X-Auth-User";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final ITokenService authTokenService;
    private final ObjectMapper objectMapper;

    /**
     * 执行 JWT 认证过滤
     *
     * @param exchange 当前请求交换
     * @param chain 过滤器链
     * @return 处理完成的 Mono
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String header = exchange.getRequest()
                                .getHeaders()
                                .getFirst("Authorization");
        if (!hasValidAuthHeader(header)) {
            // 无 Token 头 → 匿名请求，交给授权管理器判断
            return chain.filter(exchange);
        }

        String token = header.substring(TOKEN_PREFIX.length());

        return Mono.fromCallable(() -> authTokenService.parseToken(token))
                   .subscribeOn(Schedulers.boundedElastic())
                   .flatMap(userContext -> handleAuthenticated(exchange, chain, userContext))
                   .onErrorResume(AuthTokenException.class, e -> handleAuthError(exchange, e));
    }

    /**
     * 处理认证成功的情况：构建 Authentication 并设置 SecurityContext
     *
     * @param exchange 请求交换
     * @param chain 过滤器链
     * @param userContext 解析出的用户上下文
     * @return 处理完成的 Mono
     */
    private Mono<Void> handleAuthenticated(
        ServerWebExchange exchange,
        WebFilterChain chain,
        UserContext userContext
    ) {

        List<SimpleGrantedAuthority> authorities = buildAuthorities(userContext);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userContext,
                null,
                authorities
        );

        // 写入 X-Auth-User 头（下游服务透传）
        try {
            String authUserJson = objectMapper.writeValueAsString(userContext);
            ServerHttpRequest mutatedRequest = exchange.getRequest()
                                                       .mutate()
                                                       .header(AUTH_USER_HEADER, authUserJson)
                                                       .build();
            ServerWebExchange mutatedExchange = exchange.mutate()
                                                        .request(mutatedRequest)
                                                        .build();

            return chain.filter(mutatedExchange)
                        .contextWrite(
                                ctx -> ctx.put(SecurityContext.class, new SecurityContextImpl(authentication))
                        );
        } catch (JsonProcessingException e) {
            log.error("序列化 UserContext 失败", e);
            return Mono.error(e);
        }
    }

    /**
     * 处理认证失败：返回 401 Unauthorized
     *
     * @param exchange 请求交换
     * @param e 认证异常
     * @return 处理完成的 Mono
     */
    private Mono<Void> handleAuthError(ServerWebExchange exchange, AuthTokenException e) {
        log.warn("JWT 认证失败: {}", e.getMessage());
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse()
                .getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        Result<Void> result = Result.error("Token 无效或已过期", 401);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(result);
            DataBuffer buffer = exchange.getResponse()
                                        .bufferFactory()
                                        .wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException ex) {
            return exchange.getResponse().setComplete();
        }
    }

    /**
     * 从用户上下文中构建 GrantedAuthority 列表
     *
     * @param userContext 用户上下文
     * @return GrantedAuthority 列表
     */
    private List<SimpleGrantedAuthority> buildAuthorities(UserContext userContext) {
        List<String> permissionCodes = userContext.getPermissionCodes();
        if (CollUtil.isEmpty(permissionCodes)) {
            return Collections.emptyList();
        }
        return permissionCodes.stream()
                              .map(SimpleGrantedAuthority::new)
                              .collect(Collectors.toList());
    }

    /**
     * 检查 Authorization 头是否合法
     *
     * @param header Authorization 头值
     * @return true 合法
     */
    private boolean hasValidAuthHeader(String header) {
        return header != null && header.startsWith(TOKEN_PREFIX);
    }
}
