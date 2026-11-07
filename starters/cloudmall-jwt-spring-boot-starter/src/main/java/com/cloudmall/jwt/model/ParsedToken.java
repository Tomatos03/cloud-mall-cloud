package com.cloudmall.jwt.model;

import com.cloudmall.jwt.constant.JwtClaimNames;
import com.cloudmall.jwt.exception.ParseTokenException;
import com.cloudmall.jwt.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;

/**
 * 已完成签名校验的 JWT 只读对象。
 *
 * @author : Tomatos
 * @date : 2026/11/7
 */
public final class ParsedToken {

    private final Claims claims;

    /**
     * 包级构造器，只允许同包的 {@link JwtTokenService} 创建，避免包装未经校验的 Claims。
     *
     * @param claims 已完成签名与有效期校验的声明集合
     */
    ParsedToken(@NonNull Claims claims) {
        this.claims = claims;
    }

    /**
     * 将 Claims 转换为 ParsedToken。
     *
     * @param claims 已完成签名与有效期校验的声明集合
     * @return ParsedToken 对象
     */
    public static ParsedToken of(@NonNull Claims claims) {
        return new ParsedToken(claims);
    }

    /**
     * 获取用户标识：优先标准 sub 声明，兼容旧 token 的 userId 声明。
     *
     * @return 用户标识，缺失时返回 null
     */
    public String subject() {
        String subject = claims.getSubject();
        if (subject != null) {
            return subject;
        }
        Object userId = claims.get(JwtClaimNames.USER_ID);
        return userId == null ? null : String.valueOf(userId);
    }

    /**
     * 确保 token 携带用户标识。
     *
     * @param token 原始 token，用于异常上下文
     * @return 用户标识
     * @throws ParseTokenException 用户标识缺失时抛出
     */
    public String ensureSubject(String token) {
        String subject = subject();
        if (subject == null) {
            throw new ParseTokenException("token 缺少用户标识", token);
        }
        return subject;
    }

    /**
     * 获取 token 唯一标识（jti）。
     *
     * @return token 唯一标识，未设置时返回 null
     */
    public String id() {
        return claims.getId();
    }

    /**
     * 获取字符串声明。
     *
     * @param key 声明名称
     * @return 声明值，缺失时返回 null
     */
    public String getString(@NonNull String key) {
        return claims.get(key, String.class);
    }

    /**
     * 获取数值声明。
     *
     * @param key 声明名称
     * @return 声明值，缺失或非数值时返回 null
     */
    public Long getLong(@NonNull String key) {
        Object value = claims.get(key);
        return value instanceof Number number ? number.longValue() : null;
    }

    /**
     * 获取字符串列表声明。
     *
     * @param key 声明名称
     * @return 声明值，缺失或非列表时返回空列表
     */
    public List<String> getStringList(@NonNull String key) {
        Object value = claims.get(key);
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list
            .stream()
            .filter(Objects::nonNull)
            .map(String::valueOf)
            .toList();
    }

    /**
     * 获取 Map 声明。
     *
     * @param key 声明名称
     * @return 声明值，缺失或非 Map 时返回空 Map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap(@NonNull String key) {
        Object value = claims.get(key);
        return value instanceof Map<?, ?> map
            ? (Map<String, Object>) map
            : Map.of();
    }

    /**
     * 按指定类型获取声明。
     *
     * @param key 声明名称
     * @param type 目标类型
     * @param <T> 目标类型
     * @return 声明值，缺失时返回 null
     */
    public <T> T get(@NonNull String key, @NonNull Class<T> type) {
        return claims.get(key, type);
    }

    /**
     * 获取签发时间。
     *
     * @return 签发时间，未设置时返回 null
     */
    public Date issuedAt() {
        return claims.getIssuedAt();
    }

    /**
     * 获取过期时间。
     *
     * @return 过期时间，未设置时返回 null
     */
    public Date expiration() {
        return claims.getExpiration();
    }
}
