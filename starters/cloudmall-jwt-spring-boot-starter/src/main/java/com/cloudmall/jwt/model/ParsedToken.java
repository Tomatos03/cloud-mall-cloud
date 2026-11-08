package com.cloudmall.jwt.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.NonNull;

import com.cloudmall.jwt.constant.JwtClaimNames;
import com.cloudmall.jwt.exception.ParseTokenException;
import com.cloudmall.jwt.service.JwtTokenService;

/**
 * 已完成签名校验的 JWT 只读对象。
 *
 * @author : Tomatos
 * @date : 2026/11/7
 */
public final class ParsedToken {

    private final Claims claims;

    /**
     * 包级构造器，只允许同包的 {@link JwtTokenService} 创建，避免包装未经校验的
     * Claims。
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
     * @throws IllegalArgumentException 声明存在但类型不是字符串时抛出
     */
    public String getString(@NonNull String key) {
        Object value = claims.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof String string) {
            return string;
        }
        throw new IllegalArgumentException("声明 " + key + " 的类型不是字符串");
    }

    /**
     * 获取数值声明。
     *
     * @param key 声明名称
     * @return 声明值，缺失时返回 null
     * @throws IllegalArgumentException 声明存在但类型不是数值时抛出
     */
    public Long getLong(@NonNull String key) {
        Object value = claims.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalArgumentException("声明 " + key + " 的类型不是数值");
    }

    /**
     * 获取字符串列表声明。
     *
     * @param key 声明名称
     * @return 声明值，缺失时返回空列表
     * @throws IllegalArgumentException 声明存在但不是字符串列表时抛出
     */
    public List<String> getStringList(@NonNull String key) {
        Object value = claims.get(key);
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("声明 " + key + " 的类型不是列表");
        }
        List<String> result = new ArrayList<>(list.size());
        for (Object item : list) {
            if (!(item instanceof String string)) {
                throw new IllegalArgumentException("声明 " + key + " 包含非字符串元素");
            }
            result.add(string);
        }
        return List.copyOf(result);
    }

    /**
     * 获取 Map 声明。
     *
     * @param key 声明名称
     * @return 声明值，缺失时返回空 Map
     * @throws IllegalArgumentException 声明存在但类型不是 Map 时抛出
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap(@NonNull String key) {
        Object value = claims.get(key);
        if (value == null) {
            return Map.of();
        }
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new IllegalArgumentException("声明 " + key + " 的类型不是 Map");
    }

    /**
     * 按指定类型获取声明。
     *
     * @param key 声明名称
     * @param type 目标类型
     * @param <T> 目标类型
     * @return 声明值，缺失时返回 null
     * @throws IllegalArgumentException 声明存在但类型不匹配时抛出
     */
    public <T> T get(@NonNull String key, @NonNull Class<T> type) {
        try {
            return claims.get(key, type);
        } catch (JwtException e) {
            throw new IllegalArgumentException("声明 " + key + " 的类型不是 " + type.getSimpleName(), e);
        }
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
