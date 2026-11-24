package com.cloudmall.feign.decoder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import feign.Request;
import feign.Response;

import com.cloudmall.feign.exception.RemoteCallException;
import com.cloudmall.feign.exception.RemoteErrorTypeEnum;

/**
 * 远程调用异常工厂
 * <p>
 * 统一从 Feign 响应中提取服务与路径信息，构造 {@link RemoteCallException}，避免解码器与错误解码器重复拼装。
 *
 * @author : Tomatos
 */
public final class RemoteExceptionFactory {

    private static final String UNKNOWN = "unknown";

    private RemoteExceptionFactory() {}

    /**
     * 构造远端业务错误异常。
     *
     * @param response Feign 响应
     * @param remoteCode 远端业务码
     * @param message 远端错误消息
     * @return 远程调用异常
     */
    public static RemoteCallException business(Response response, int remoteCode, String message) {
        return of(response, RemoteErrorTypeEnum.BUSINESS, remoteCode, message, null);
    }

    /**
     * 构造指定类型的远程调用异常。
     *
     * @param response Feign 响应
     * @param type 失败类型
     * @param remoteCode 业务码或通用错误码
     * @param message 错误消息
     * @param cause 原始异常
     * @return 远程调用异常
     */
    public static RemoteCallException of(
        Response response,
        RemoteErrorTypeEnum type,
        int remoteCode,
        String message,
        Throwable cause
    ) {
        Request request = response.request();
        if (request == null) {
            request = Request.create(
                    Request.HttpMethod.GET,
                    "http://unknown",
                    Collections.emptyMap(),
                    new byte[0],
                    StandardCharsets.UTF_8
            );
        }

        String service = UNKNOWN;
        String path = UNKNOWN;
        try {
            URI uri = URI.create(request.url());
            service = uri.getHost() == null ? UNKNOWN : uri.getHost();
            path = uri.getPath() == null ? UNKNOWN : uri.getPath();
        } catch (IllegalArgumentException ignored) {
            // 非标准 URL 时保留 unknown，不影响异常主流程
        }

        return new RemoteCallException(
                type,
                response.status(),
                message,
                remoteCode,
                service,
                path,
                request,
                cause
        );
    }
}
