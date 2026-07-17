package com.cloudmall.common.entity;

import lombok.Data;

/**
 * 统一返回值工具类
 *
 * @author Tomatos
 * @date 2025/12/17
 */
@Data
public class Result<T> {
    public static final int SUCCESS_CODE = 0;
    public static final int INTERNAL_ERROR_CODE = 500;

    /**
     * 状态码
     */
    private int code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应
     * @param data 数据
     * @return Result
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, "操作成功", data);
    }

    /**
     * 成功响应
     * @param message 消息
     * @param data 数据
     * @return Result
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(SUCCESS_CODE, message, data);
    }

    /**
     * 错误响应
     * @param message 消息
     * @return Result
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(INTERNAL_ERROR_CODE, message, null);
    }

    /**
     * 错误响应
     * @param message 消息
     * @param code 错误码
     * @return Result
     */
    public static <T> Result<T> error(String message, int code) {
        return new Result<>(code, message, null);
    }
}
