package com.cloudmall.feign.exception;

import feign.FeignException;
import feign.Request;
import lombok.Getter;

/**
 * 远程调用统一异常
 * <p>
 * 继承 {@link FeignException} 以免被 Feign 再次包装为 {@code DecodeException}，
 * 从而能被全局异常处理器按类型精确映射。远端业务错误码、服务与路径保存在异常上下文中。
 *
 * @author : Tomatos
 */
@Getter
public class RemoteCallException extends FeignException {

    private final RemoteErrorTypeEnum type;

    private final int remoteCode;

    private final String service;

    private final String path;

    /**
     * 构造远程调用异常。
     *
     * @param type 失败类型
     * @param status HTTP 状态
     * @param message 错误消息
     * @param remoteCode 远端业务码，非业务错误时取对应通用错误码
     * @param service 远端服务标识
     * @param path 请求路径
     * @param request Feign 请求，不能为 null
     * @param cause 原始异常
     */
    public RemoteCallException(
        RemoteErrorTypeEnum type,
        int status,
        String message,
        int remoteCode,
        String service,
        String path,
        Request request,
        Throwable cause
    ) {
        super(status, message, request, cause);
        this.type = type;
        this.remoteCode = remoteCode;
        this.service = service;
        this.path = path;
    }
}
