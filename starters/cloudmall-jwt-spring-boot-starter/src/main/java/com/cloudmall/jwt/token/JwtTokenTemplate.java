package com.cloudmall.jwt.token;

import com.cloudmall.context.UserContext;
import com.cloudmall.jwt.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtTokenTemplate {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenTemplate(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.expirationMs = properties.getExpiration();
    }

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

    @SuppressWarnings("unchecked")
    public UserContext parse(String token) {
        Claims claims = Jwts.parser()
                            .verifyWith(key)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();
        return UserContext
                .builder()
                .userId(claims.get("userId", Long.class))
                .username(claims.get("username", String.class))
                .userType(claims.get("userType", String.class))
                .extra(claims.get("extra", Map.class))
                .build();
    }
}
