package com.cloudmall.common.exception;

import lombok.Getter;

import com.cloudmall.common.enums.BizErrorCode;

/**
 * 业务异常
 * <p>
 * 使用 {@link BizErrorCode} 枚举统一管理错误码和错误消息，
 * 禁止直接抛出 {@link RuntimeException} 或 {@link Exception}。
 *
 * @author : Tomatos
 */
@Getter
public class BizException extends RuntimeException {
    private BizErrorCode bizErrorCode;

    /**
     * 使用指定参数构造对象。
     *
     * @param bizErrorCode 业务错误码
     */
    public BizException(BizErrorCode bizErrorCode) {
        super(bizErrorCode.getErrorMessage());
        this.bizErrorCode = bizErrorCode;
    }
}
