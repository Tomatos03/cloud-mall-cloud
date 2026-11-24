package com.cloudmall.feign.decoder;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.feign.exception.RemoteCallException;
import com.cloudmall.feign.exception.RemoteErrorTypeEnum;

/**
 * 远程调用错误解码器
 * <p>
 * 非 2xx 响应由本类统一翻译。优先解析响应体中的 Result 信封以保留远端业务码；
 * 解析失败时按 HTTP 状态归类为超时、限流、未找到或不可用。
 *
 * @author : Tomatos
 */
@RequiredArgsConstructor
public class RemoteErrorDecoder implements ErrorDecoder {

    private static final int STATUS_REQUEST_TIMEOUT = 408;

    private static final int STATUS_TOO_MANY_REQUESTS = 429;

    private static final int STATUS_NOT_FOUND = 404;

    private static final int STATUS_GATEWAY_TIMEOUT = 504;

    private final ResultParser parser;

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            JsonNode envelope = parser.parse(response);
            if (!parser.success(envelope)) {
                return RemoteExceptionFactory.business(
                        response,
                        parser.code(envelope),
                        parser.message(envelope, "远程服务调用失败")
                );
            }
        } catch (IOException | DecodeException ignored) {
            // 响应体不是 Result 信封，继续按 HTTP 状态映射
        }

        return byStatus(response);
    }

    private RemoteCallException byStatus(Response response) {
        int status = response.status();

        if (status == STATUS_REQUEST_TIMEOUT || status == STATUS_GATEWAY_TIMEOUT) {
            return RemoteExceptionFactory.of(
                    response,
                    RemoteErrorTypeEnum.TIMEOUT,
                    BizErrorEnum.GATEWAY_TIMEOUT.getCode(),
                    BizErrorEnum.GATEWAY_TIMEOUT.getMessage(),
                    null
            );
        }

        if (status == STATUS_TOO_MANY_REQUESTS) {
            return RemoteExceptionFactory.of(
                    response,
                    RemoteErrorTypeEnum.REJECTED,
                    BizErrorEnum.FLOW_CONTROL_BLOCKED.getCode(),
                    BizErrorEnum.FLOW_CONTROL_BLOCKED.getMessage(),
                    null
            );
        }

        if (status == STATUS_NOT_FOUND) {
            return RemoteExceptionFactory.of(
                    response,
                    RemoteErrorTypeEnum.NOT_FOUND,
                    BizErrorEnum.DATA_NOT_FOUND.getCode(),
                    BizErrorEnum.DATA_NOT_FOUND.getMessage(),
                    null
            );
        }

        if (status >= 500) {
            return RemoteExceptionFactory.of(
                    response,
                    RemoteErrorTypeEnum.SERVER,
                    BizErrorEnum.SERVICE_UNAVAILABLE.getCode(),
                    BizErrorEnum.SERVICE_UNAVAILABLE.getMessage(),
                    null
            );
        }

        return RemoteExceptionFactory.of(response, RemoteErrorTypeEnum.BUSINESS, status, "远程服务返回错误", null);
    }
}
