package com.cloudmall.gateway.config;

import java.util.Locale;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.cloudmall.gateway.filter.JwtAuthenticationWebFilter;
import com.cloudmall.gateway.filter.RbacReactiveAuthorizationManager;
import com.cloudmall.gateway.properties.AuthProperties;

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
     * 构建 Spring Security 过滤器链 <p>
     * 配置白名单路径免认证、RBAC 授权管理器和 JWT 认证过滤器。
     *
     * @param http ServerHttpSecurity 构建器
     * @param jwtAuthenticationWebFilter JWT 认证过滤器
     * @param rbacAuthorizationManager RBAC 授权管理器
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
                .authorizeExchange(
                        exchanges -> {
                            permitWhitelist(exchanges);
                            exchanges.anyExchange().access(rbacAuthorizationManager);
                        }
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .addFilterBefore(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    /**
     * 注册白名单免认证规则 <p>
     * 条目支持两种写法：{@code "METHOD:path"} 只放行指定方法，
     * {@code "path"} 放行全部方法。 方法限定是必要的：秒杀活动列表与详情对匿名开放，但同路
     * 径的 POST/PATCH 属于运营操作，必须继续走 RBAC。
     *
     * @param exchanges 授权规则构建器
     */
    private void permitWhitelist(AuthorizeExchangeSpec exchanges) {
        for (String entry : authProperties.getWhitelist()) {
            String[] parts = entry.split(":", 2);
            if (parts.length == 2) {
                exchanges.pathMatchers(HttpMethod.valueOf(parts[0].toUpperCase(Locale.ROOT)), parts[1])
                         .permitAll();
            } else {
                exchanges.pathMatchers(entry).permitAll();
            }
        }
    }
}
