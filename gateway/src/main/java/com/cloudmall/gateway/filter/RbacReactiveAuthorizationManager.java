package com.cloudmall.gateway.filter;

import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import com.cloudmall.gateway.config.AuthProperties;

/**
 * RBAC 响应式授权管理器
 * <p>
 * 实现 ReactiveAuthorizationManager，在 Gateway 层统一进行权限校验。
 * 根据 YAML 中配置的 URL-权限映射规则，检查当前用户的 Authentication
 * 是否包含所需的权限编码。
 * <p>
 * 白名单路径由 GatewaySecurityConfig 的 pathMatchers().permitAll() 管理，
 * 本授权管理器仅处理非白名单路径的权限校验。
 * <p>
 * 授权决策逻辑：
 * <ul>
 *     <li>无匹配规则 → 放行</li>
 *     <li>匹配规则 → 用户 authorities 包含任一所需权限 → 放行，否则拒绝</li>
 * </ul>
 *
 * @author Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RbacReactiveAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    private final AuthProperties authProperties;

    /**
     * 执行授权检查
     *
     * @param authentication Mono 包装的当前用户认证信息
     * @param context 授权上下文（包含请求信息）
     * @return 授权决策
     */
    @Override
    public Mono<AuthorizationDecision> check(
        Mono<Authentication> authentication,
        AuthorizationContext context
    ) {

        String method = context.getExchange()
                               .getRequest()
                               .getMethod()
                               .name();
        String path = context.getExchange()
                             .getRequest()
                             .getURI()
                             .getPath();

        // 匹配路径规则
        AuthProperties.UrlPermission matched = authProperties.match(method, path);
        if (matched == null) {
            // 未匹配任何规则 → 默认放行
            return Mono.just(new AuthorizationDecision(true));
        }

        // 有匹配规则，检查用户权限
        return authentication.map(auth -> checkPermissions(auth, matched))
                             .defaultIfEmpty(new AuthorizationDecision(false));
    }

    /**
     * 检查用户是否拥有规则所需的任一权限
     *
     * @param auth 用户认证信息
     * @param matched 匹配的权限规则
     * @return 授权决策
     */
    private AuthorizationDecision checkPermissions(Authentication auth, AuthProperties.UrlPermission matched) {
        Set<String> userPermissions = auth.getAuthorities()
                                          .stream()
                                          .map(GrantedAuthority::getAuthority)
                                          .collect(Collectors.toSet());

        // 检查用户是否拥有任一所需权限（OR 逻辑）
        boolean granted = matched.getPermissions()
                                 .stream()
                                 .anyMatch(userPermissions::contains);

        if (!granted) {
            log.warn("权限不足: 用户 authorities={}, 所需权限={}", userPermissions, matched.getPermissions());
        }

        return new AuthorizationDecision(granted);
    }
}
