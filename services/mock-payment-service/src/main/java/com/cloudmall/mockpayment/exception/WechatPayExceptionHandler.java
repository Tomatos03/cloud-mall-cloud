package com.cloudmall.mockpayment.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.mockpayment.controller.WechatPayController;
import com.cloudmall.mockpayment.model.resp.WechatErrorResp;

/**
 * 微信支付协议异常处理器。
 * <p>
 * 仅作用于 /v3/** 模拟协议控制器，将业务异常转换为微信支付风格
 * {@code {"code":"PARAM_ERROR","message":"..."}}，避免业务系统按 Result 解析失败。
 *
 * @author : Tomatos
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = WechatPayController.class)
public class WechatPayExceptionHandler {

    /**
     * 业务异常。
     *
     * @param e 业务异常
     * @return 微信风格错误响应
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<WechatErrorResp> handleBiz(BizException e) {
        WechatApiError error = map(e.getBizError());
        return ResponseEntity.status(error.status()).body(new WechatErrorResp(error.code(), error.message()));
    }

    /**
     * 参数校验失败。
     *
     * @param e 校验异常
     * @return 微信风格错误响应
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<WechatErrorResp> handleValidation(Exception e) {
        return paramError("参数格式错误");
    }

    /**
     * 缺少请求参数。
     *
     * @param e 参数缺失异常
     * @return 微信风格错误响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<WechatErrorResp> handleMissingParam(MissingServletRequestParameterException e) {
        return paramError("缺少必要参数: " + e.getParameterName());
    }

    /**
     * 请求报文不可读。
     *
     * @param e 报文异常
     * @return 微信风格错误响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<WechatErrorResp> handleUnreadable(HttpMessageNotReadableException e) {
        return paramError("请求报文格式错误");
    }

    /**
     * 兜底异常。
     *
     * @param e 异常
     * @return 微信风格错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<WechatErrorResp> handleUnknown(Exception e) {
        log.error("模拟支付协议接口内部异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(new WechatErrorResp("SYSTEM_ERROR", "系统异常，请稍后重试"));
    }

    /**
     * 构造参数错误响应。
     *
     * @param message 错误信息
     * @return 错误响应
     */
    private static ResponseEntity<WechatErrorResp> paramError(String message) {
        return ResponseEntity.badRequest().body(new WechatErrorResp("PARAM_ERROR", message));
    }

    /**
     * 将业务错误码映射为微信支付错误码。
     *
     * @param error 业务错误码
     * @return 微信支付错误
     */
    private static WechatApiError map(BizErrorEnum error) {
        return switch (error) {
            case INVALID_PARAM, MOCK_PAYMENT_AMOUNT_INVALID -> new WechatApiError(
                    "PARAM_ERROR",
                    HttpStatus.BAD_REQUEST.value(),
                    error.getMessage()
            );
            case MOCK_PAYMENT_ORDER_NOT_EXIST, DATA_NOT_FOUND -> new WechatApiError(
                    "ORDER_NOT_EXIST",
                    HttpStatus.NOT_FOUND.value(),
                    error.getMessage()
            );
            case MOCK_PAYMENT_ORDER_CLOSED -> new WechatApiError(
                    "ORDER_CLOSED",
                    HttpStatus.BAD_REQUEST.value(),
                    error.getMessage()
            );
            case MOCK_PAYMENT_ORDER_PAID -> new WechatApiError(
                    "ORDERPAID",
                    HttpStatus.BAD_REQUEST.value(),
                    error.getMessage()
            );
            case MOCK_PAYMENT_OUT_TRADE_NO_USED -> new WechatApiError(
                    "OUT_TRADE_NO_USED",
                    HttpStatus.BAD_REQUEST.value(),
                    error.getMessage()
            );
            case MOCK_PAYMENT_SIGNATURE_INVALID -> new WechatApiError(
                    "SIGN_ERROR",
                    HttpStatus.UNAUTHORIZED.value(),
                    error.getMessage()
            );
            default -> new WechatApiError(
                    "SYSTEM_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    error.getMessage()
            );
        };
    }

    /**
     * 微信支付错误。
     *
     * @param code 错误码
     * @param status HTTP 状态码
     * @param message 错误信息
     */
    private record WechatApiError(String code, int status, String message) {
    }
}
