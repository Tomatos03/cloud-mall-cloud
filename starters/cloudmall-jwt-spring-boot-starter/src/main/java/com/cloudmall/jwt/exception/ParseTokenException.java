package com.cloudmall.jwt.exception;

import lombok.Getter;

/**
 * 令牌解析异常
 * <p>
 * 当 token 解析失败、签名无效、已过期或缺少用户标识时抛出。
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
@Getter
public class ParseTokenException extends RuntimeException {

    private final String token;

    /**
     * 使用指定参数构造对象。
     *
     * @param message 消息
     * @param token 令牌
     */
    public ParseTokenException(String message, String token) {
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
    public ParseTokenException(String message, String token, Throwable cause) {
        super(message, cause);
        this.token = token;
    }
}
