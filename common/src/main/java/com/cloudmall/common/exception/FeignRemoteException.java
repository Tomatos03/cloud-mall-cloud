package com.cloudmall.common.exception;

import lombok.Getter;

/**
 * 远程服务调用异常
 * <p>
 * Feign Decoder 拆箱 Result 时，若 code ≠ SUCCESS 则抛出此异常，
 * 携带远程服务的原始错误码与消息，由全局异常处理器统一转换为 Result 响应。
 *
 * @author : Tomatos
 */
@Getter
public class FeignRemoteException extends RuntimeException {

    private final int remoteCode;

    public FeignRemoteException(int remoteCode, String message) {
        super(message);
        this.remoteCode = remoteCode;
    }
}
