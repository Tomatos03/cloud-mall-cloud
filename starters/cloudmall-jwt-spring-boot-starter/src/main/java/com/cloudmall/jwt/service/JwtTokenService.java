package com.cloudmall.jwt.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;

import cn.hutool.core.util.StrUtil;
import com.cloudmall.jwt.model.ParsedToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import com.cloudmall.jwt.config.JwtProperties;
import com.cloudmall.jwt.exception.ParseTokenException;

/**
 * 基于 JJWT 的认证令牌实现
 * <p>
 * 使用 HMAC-SHA 签名，声明由调用方通过 Map 传入，常用声明见
 * {@link com.cloudmall.jwt.constant.JwtClaimNames}。
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
public class JwtTokenService implements ITokenService {

    private final JwtProperties properties;

    /**
     * 构造 JWT Token 服务
     *
     * @param properties JWT 配置属性
     */
    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
    }

    /**
     * 将配置中的密钥转换为 HMAC-SHA 签名密钥。
     *
     * @return 签名密钥
     */
    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 创建 JWT Token <p>
     * 将声明集合编码为 JWT 字符串，值为 null 的声明会被忽略。
     *
     * @param claims 声明集合
     * @return JWT token 字符串
     */
    @Override
    public String createToken(Map<String, Object> claims) {
        Date now = new Date();
        JwtBuilder builder = Jwts.builder()
                                 .issuedAt(now)
                                 .expiration(new Date(now.getTime() + properties.getExpiration()))
                                 .signWith(secretKey());

        claims.forEach((name, value) -> {
            if (StrUtil.isNotBlank(name) && value != null) {
                builder.claim(name, value);
            }
        });

        return builder.compact();
    }

    /**
     * 解析 Token <p>
     * 校验签名与有效期后返回只读的 {@link ParsedToken}。
     *
     * @param token JWT token 字符串
     * @return 解析后的 token
     * @throws ParseTokenException token 无效、已过期或缺少用户标识时抛出
     */
    @Override
    public ParsedToken parseToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                         .verifyWith(secretKey())
                         .build()
                         .parseSignedClaims(token)
                         .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new ParseTokenException("token 无效或已过期", token, e);
        }
        ParsedToken parsedToken = ParsedToken.of(claims);
        parsedToken.ensureSubject(token);
        return parsedToken;
    }
}
