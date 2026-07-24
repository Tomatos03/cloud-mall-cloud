package com.cloudmall.jwt.token;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import com.cloudmall.auth.AuthTokenException;
import com.cloudmall.auth.AuthTokenService;
import com.cloudmall.context.UserContext;
import com.cloudmall.jwt.JwtProperties;

/**
 * 基于 JJWT 的认证令牌实现
 * <p>
 * 使用 HMAC-SHA 签名，token 中携带 userId、username、userType 和扩展字段。
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
public class JwtTokenService implements AuthTokenService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenService(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.expirationMs = properties.getExpiration();
    }

    @Override
    public String createToken(UserContext userContext) {
        Date now = new Date();
        return Jwts.builder()
                   .claim("userId", userContext.getUserId())
                   .claim("username", userContext.getUsername())
                   .claim("userType", userContext.getUserType())
                   .claims(userContext.getExtra())
                   .issuedAt(now)
                   .expiration(new Date(now.getTime() + expirationMs))
                   .signWith(key)
                   .compact();
    }

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
        return UserContext
                .builder()
                .userId(claims.get("userId", Long.class))
                .username(claims.get("username", String.class))
                .userType(claims.get("userType", String.class))
                .extra(claims.get("extra", Map.class))
                .build();
    }
}
