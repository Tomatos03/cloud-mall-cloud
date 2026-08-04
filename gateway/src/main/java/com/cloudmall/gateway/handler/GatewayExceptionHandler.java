package com.cloudmall.gateway.handler;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.model.Result;

/**
 * Gateway 全局异常处理器
 * 将 Gateway 层面的异常统一转换为 Result 格式
 *
 * @author Tomatos
 */
@Slf4j
@Order(-1)
@Configuration
@RequiredArgsConstructor
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    /**
     * 处理相关异常。
     *
     * @param exchange HTTP 交换对象
     * @param ex 异常对象
     * @return 处理结果
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        int businessCode;
        HttpStatus httpStatus;
        String message;

        if (ex instanceof NotFoundException) {
            // 路由找不到（下游服务未注册等）
            businessCode = BizError.SERVICE_UNAVAILABLE.getCode();
            httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
            message = BizError.SERVICE_UNAVAILABLE.getErrorMessage();
        } else if (ex instanceof ResponseStatusException statusException) {
            // 其他已知状态异常
            httpStatus = HttpStatus.resolve(statusException.getStatusCode().value());
            if (httpStatus == null) {
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            businessCode = httpStatus.is5xxServerError() ? Result.INTERNAL_ERROR_CODE : httpStatus.value();
            message = statusException.getReason();
            if (StrUtil.isBlank(message)) {
                message = "服务器内部错误";
            }
        } else if (ex instanceof TimeoutException || ex instanceof ConnectException) {
            // 超时或连接异常
            businessCode = BizError.GATEWAY_TIMEOUT.getCode();
            httpStatus = HttpStatus.GATEWAY_TIMEOUT;
            message = BizError.GATEWAY_TIMEOUT.getErrorMessage();
        } else {
            // 兜底
            businessCode = Result.INTERNAL_ERROR_CODE;
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "服务器内部错误";
        }

        log.error("Gateway 异常: http={} bizCode={} {}", httpStatus, businessCode, message, ex);

        return writeJsonResponse(exchange.getResponse(), httpStatus, businessCode, message);
    }

    /**
     * 将业务错误信息写入 JSON 响应。
     *
     * @param response HTTP 响应对象
     * @param httpStatus HTTP 状态码
     * @param businessCode 业务错误码
     * @param message 消息
     * @return 处理结果
     */
    private Mono<Void> writeJsonResponse(
            ServerHttpResponse response,
            HttpStatus httpStatus,
            int businessCode,
            String message
    ) {
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Void> result = Result.error(message, businessCode);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsString(result).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            log.error("序列化异常响应失败", e);
            bytes = ("{\"code\":500,\"message\":\"服务器内部错误\",\"data\":null}").getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
