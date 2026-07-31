package com.cloudmall.jwt.exception;

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

    /**
     * 使用指定参数构造对象。
     *
     * @param message 消息
     * @param token 令牌
     */
    public AuthTokenException(String message, String token) {
        super(message);
        this.token = token;
    }

    /**
     * 使用指定参数构造对象。
     *
     * @param message 消息
     * @param token 令牌
     * @param cause 原始异常
     */
    public AuthTokenException(String message, String token, Throwable cause) {
        super(message, cause);
        this.token = token;
    }
}
