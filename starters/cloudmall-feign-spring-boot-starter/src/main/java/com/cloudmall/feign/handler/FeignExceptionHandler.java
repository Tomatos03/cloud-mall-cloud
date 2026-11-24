package com.cloudmall.feign.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import feign.FeignException;
import feign.codec.DecodeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.model.result.Result;
import com.cloudmall.feign.exception.RemoteCallException;
import com.cloudmall.feign.exception.RemoteErrorTypeEnum;

/**
 * Feign 远程调用异常映射
 * <p>
 * 与通用 {@code GlobalExceptionHandler} 分离，避免 Feign 类型污染 common 模块。
 * 使用最高优先级确保远程调用异常先于通用 Exception handler 被处理。
 *
 * @author : Tomatos
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class FeignExceptionHandler {

    /**
     * 远程调用统一异常。
     *
     * @param e 异常对象
     * @return 处理结果
     */
    @ExceptionHandler(RemoteCallException.class)
    public ResponseEntity<Result<Void>> handleRemoteCall(RemoteCallException e) {
        log.warn(
                "远程服务调用失败: type={}, service={}, path={}, code={}, message={}",
                e.getType(),
                e.getService(),
                e.getPath(),
                e.getRemoteCode(),
                e.getMessage()
        );

        int code = switch (e.getType()) {
            case BUSINESS -> e.getRemoteCode();
            case TIMEOUT -> BizErrorEnum.GATEWAY_TIMEOUT.getCode();
            case REJECTED -> BizErrorEnum.FLOW_CONTROL_BLOCKED.getCode();
            case NOT_FOUND -> e.getRemoteCode() == 0
                    ? BizErrorEnum.DATA_NOT_FOUND.getCode()
                    : e.getRemoteCode();
            default -> BizErrorEnum.SERVICE_UNAVAILABLE.getCode();
        };
        return ResponseEntity.status(status(e.getType())).body(Result.error(e.getMessage(), code));
    }

    /**
     * 解码异常，解包远程调用异常后按统一规则映射。
     *
     * @param e 异常对象
     * @return 处理结果
     */
    @ExceptionHandler(DecodeException.class)
    public ResponseEntity<Result<Void>> handleDecode(DecodeException e) {
        if (e.getCause() instanceof RemoteCallException remote) {
            return handleRemoteCall(remote);
        }

        log.error("远程响应解码失败", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(Result.error("远程响应解码失败", Result.INTERNAL_ERROR_CODE));
    }

    /**
     * Sentinel 限流或熔断拦截。
     *
     * @param e 异常对象
     * @return 处理结果
     */
    @ExceptionHandler(BlockException.class)
    public ResponseEntity<Result<Void>> handleBlock(BlockException e) {
        log.warn("远程调用被 Sentinel 拦截: message={}", e.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                             .body(Result.error(BizErrorEnum.FLOW_CONTROL_BLOCKED));
    }

    /**
     * Feign 兜底异常，覆盖负载均衡或传输层未经过错误解码器的失败。
     *
     * @param e 异常对象
     * @return 处理结果
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Result<Void>> handleFeign(FeignException e) {
        log.error("Feign 调用失败: status={}", e.status(), e);

        BizErrorEnum error = switch (e.status()) {
            case 408, 504 -> BizErrorEnum.GATEWAY_TIMEOUT;
            case 429 -> BizErrorEnum.FLOW_CONTROL_BLOCKED;
            case 404 -> BizErrorEnum.DATA_NOT_FOUND;
            default -> BizErrorEnum.SERVICE_UNAVAILABLE;
        };
        return ResponseEntity.status(status(typeOf(error))).body(Result.error(error));
    }

    private RemoteErrorTypeEnum typeOf(BizErrorEnum error) {
        return switch (error) {
            case GATEWAY_TIMEOUT -> RemoteErrorTypeEnum.TIMEOUT;
            case FLOW_CONTROL_BLOCKED -> RemoteErrorTypeEnum.REJECTED;
            case DATA_NOT_FOUND -> RemoteErrorTypeEnum.NOT_FOUND;
            default -> RemoteErrorTypeEnum.SERVER;
        };
    }

    private HttpStatus status(RemoteErrorTypeEnum type) {
        return switch (type) {
            case BUSINESS -> HttpStatus.BAD_REQUEST;
            case TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
            case REJECTED -> HttpStatus.TOO_MANY_REQUESTS;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.SERVICE_UNAVAILABLE;
        };
    }
}
