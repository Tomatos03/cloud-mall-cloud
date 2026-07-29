package com.cloudmall.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.cloudmall.gateway.filter.JwtAuthenticationWebFilter;
import com.cloudmall.gateway.filter.RbacReactiveAuthorizationManager;

/**
 * Gateway Spring Security 安全配置
 * <p>
 * 配置 JWT 认证过滤器与 RBAC 授权管理器。
 * 白名单路径通过 pathMatchers().permitAll() 免认证放行，
 * 其余请求统一走 RbacReactiveAuthorizationManager 进行权限校验。
 *
 * @author Tomatos
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class GatewaySecurityConfig {

    private final AuthProperties authProperties;

    /**
     * 构建 Spring Security 过滤器链
     * <p>
     * 配置白名单路径免认证、RBAC 授权管理器和 JWT 认证过滤器。
     *
     * @param http                       ServerHttpSecurity 构建器
     * @param jwtAuthenticationWebFilter JWT 认证过滤器
     * @param rbacAuthorizationManager   RBAC 授权管理器
     * @return 构建完成的 SecurityWebFilterChain
     */
    @Bean
    public SecurityWebFilterChain filterChain(
            ServerHttpSecurity http,
            JwtAuthenticationWebFilter jwtAuthenticationWebFilter,
            RbacReactiveAuthorizationManager rbacAuthorizationManager
    ) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(authProperties.getWhitelist().toArray(new String[0]))
                            .permitAll()
                        .anyExchange()
                            .access(rbacAuthorizationManager)
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .addFilterBefore(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
