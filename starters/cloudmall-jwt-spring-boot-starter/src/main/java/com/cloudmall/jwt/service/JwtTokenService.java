package com.cloudmall.jwt.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;

import cn.hutool.core.collection.CollUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import com.cloudmall.common.context.UserContext;
import com.cloudmall.common.enums.UserTypeEnum;
import com.cloudmall.jwt.config.JwtProperties;
import com.cloudmall.jwt.exception.AuthTokenException;

/**
 * 基于 JJWT 的认证令牌实现
 * <p>
 * 使用 HMAC-SHA 签名，token 中携带 userId、username、userType、shopId、permissionCodes 和扩展字段。
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
public class JwtTokenService implements ITokenService {

    private final SecretKey key;
    private final long expirationMs;

    /**
     * 构造 JWT Token 服务
     *
     * @param properties JWT 配置属性
     */
    public JwtTokenService(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.expirationMs = properties.getExpiration();
    }

    /**
     * 创建 JWT Token <p>
     * 将用户上下文编码为 JWT 字符串，包含 userId、username、userType、
     * shopId、permissionCodes 和扩展字段。
     *
     * @param userContext 用户上下文
     * @return JWT token 字符串
     */
    @Override
    public String createToken(UserContext userContext) {
        Date now = new Date();
        Map<String, Object> extra = userContext.getExtra();
        List<String> permissionCodes = userContext.getPermissionCodes();

        JwtBuilder builder = Jwts.builder()
                                 .claim("userId", userContext.getUserId())
                                 .claim("username", userContext.getUsername())
                                 .claim("userType", userContext.getUserType())
                                 .issuedAt(now)
                                 .expiration(new Date(now.getTime() + expirationMs))
                                 .signWith(key);

        if (userContext.getShopId() != null) {
            builder.claim("shopId", userContext.getShopId());
        }

        if (CollUtil.isNotEmpty(extra)) {
            builder.claims().add(extra);
        }

        if (CollUtil.isNotEmpty(permissionCodes)) {
            builder.claim("permissionCodes", permissionCodes);
        }

        return builder.compact();
    }

    /**
     * 校验 Token 是否合法 <p>
     * 仅验证签名和有效期，不还原用户上下文。
     *
     * @param token JWT token 字符串
     * @return true 合法，false 无效或已过期
     */
    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 解析 Token <p>
     * 从 JWT 字符串中还原用户上下文，包含 permissionCodes。
     *
     * @param token JWT token 字符串
     * @return 用户上下文
     * @throws AuthTokenException token 无效或已过期时抛出
     */
    @Override
    @SuppressWarnings("unchecked")
    public UserContext parseToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                         .verifyWith(key)
                         .build()
                         .parseSignedClaims(token)
                         .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthTokenException("token 无效或已过期", token, e);
        }
        UserContext.UserContextBuilder builder = UserContext
                .builder()
                .userId(claims.get("userId", Long.class))
                .username(claims.get("username", String.class))
                .userType(UserTypeEnum.fromCodeOrNull(claims.get("userType", String.class)))
                .shopId(claims.get("shopId", Long.class))
                .extra(claims.get("extra", Map.class));

        Object permissionCodesObj = claims.get("permissionCodes");
        if (permissionCodesObj instanceof List) {
            builder.permissionCodes((List<String>) permissionCodesObj);
        }

        return builder.build();
    }
}
