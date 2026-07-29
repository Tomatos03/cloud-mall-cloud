package com.cloudmall.gateway.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.AntPathMatcher;


/**
 * 认证与权限配置属性
 * <p>
 * 包含白名单路径和 URL-权限编码映射规则。
 * urlPermissions 定义了每个 HTTP 方法+路径需要哪些权限编码才能访问。
 *
 * @author Tomatos
 */
@Data
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 白名单路径，无需认证即可访问
     */
    private List<String> whitelist = List.of("/auth/login", "/auth/register");

    /**
     * URL-权限映射规则，按顺序匹配，第一个匹配的生效
     */
    private List<UrlPermission> urlPermissions = new ArrayList<>();

    /**
     * URL 权限映射规则
     */
    @Data
    public static class UrlPermission {

        /**
         * 匹配模式，格式为 "METHOD:path"，如 "GET:/goods/**"
         * METHOD 支持 * 通配（任意 HTTP 方法）
         */
        private String pattern;

        /**
         * 所需权限编码列表（OR 关系，满足任一即放行）
         */
        private List<String> permissions = new ArrayList<>();
    }

    /**
     * 判断路径是否在白名单中
     *
     * @param path 请求路径
     * @return true 在白名单中
     */
    public boolean isWhitelisted(String path) {
        return whitelist.stream().anyMatch(w -> PATH_MATCHER.match(w, path));
    }

    /**
     * 匹配当前请求的 URL 权限规则
     * <p>
     * 按列表顺序匹配，返回第一个匹配的规则。未匹配返回 null。
     *
     * @param method HTTP 方法
     * @param path   请求路径
     * @return 匹配的权限规则，无匹配返回 null
     */
    public UrlPermission match(String method, String path) {
        return urlPermissions.stream()
                             .filter(up -> matchPattern(up.getPattern(), method, path))
                             .findFirst()
                             .orElse(null);
    }

    /**
     * 判断请求是否匹配给定的 pattern
     *
     * @param pattern 模式串，格式 "METHOD:path"
     * @param method  HTTP 方法
     * @param path    请求路径
     * @return true 匹配
     */
    private boolean matchPattern(String pattern, String method, String path) {
        String[] parts = pattern.split(":", 2);
        if (parts.length != 2) {
            return false;
        }
        String patternMethod = parts[0];
        String patternPath = parts[1];

        // 方法匹配：* 通配或精确匹配
        if (!"*".equals(patternMethod) && !patternMethod.equalsIgnoreCase(method)) {
            return false;
        }

        // 路径匹配：使用 AntPathMatcher
        return PATH_MATCHER.match(patternPath, path);
    }
}
