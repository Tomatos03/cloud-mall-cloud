package com.cloudmall.auth;

import lombok.Getter;

/**
 * 认证令牌异常
 * <p>
 * 当 token 解析失败、签名无效或已过期时抛出。
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
@Getter
public class AuthTokenException extends RuntimeException {

    private final String token;

    public AuthTokenException(String message, String token) {
        super(message);
        this.token = token;
    }

    public AuthTokenException(String message, String token, Throwable cause) {
        super(message, cause);
        this.token = token;
    }
}
