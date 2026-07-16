package com.cloudmall.im.security;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT Token 提供者
 *
 * @author : Tomatos
 * @date : 2026/2/17
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:cloud-mall-jwt-secret-key-2026-very-long-secret-key}")
    private String jwtSecret;

    private SecretKey key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解析 token 并返回 ParsedToken
     *
     * @param token JWT token
     * @return 解析后的 token 信息，解析失败返回 null
     */
    public ParsedToken parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = claims.get("userId", Long.class);
            String username = claims.get("username", String.class);
            Long storeId = claims.get("storeId", Long.class);
            String accountType = claims.get("userType", String.class);

            return new ParsedToken(userId, username, Collections.emptyList(), storeId, accountType);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}