package com.cloudmall.common.exception;

import lombok.Getter;

import com.cloudmall.common.enums.BizErrorEnum;

/**
 * 业务异常
 * <p>
 * 使用 {@link BizErrorEnum} 枚举统一管理错误码和错误消息，
 * 禁止直接抛出 {@link RuntimeException} 或 {@link Exception}。
 *
 * @author : Tomatos
 */
@Getter
public class BizException extends RuntimeException {
    private BizErrorEnum bizError;

    /**
     * 使用指定参数构造对象。
     *
     * @param bizError 业务错误码
     */
    public BizException(BizErrorEnum bizError) {
        super(bizError.getMessage());
        this.bizError = bizError;
    }
}
