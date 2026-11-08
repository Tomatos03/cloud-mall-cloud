package com.cloudmall.order.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.model.Result;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.application.PaymentApp;
import com.cloudmall.order.model.resp.PaymentCreateResp;
import com.cloudmall.order.model.resp.PaymentStatusResp;
import com.cloudmall.order.model.resp.WechatNotifyResp;

/**
 * 订单支付接口。
 * <p>
 * 用户发起支付/查询支付状态；/orders/payment/notify/wechat 供第三方支付渠道
 * 直接回调，验签失败返回 401，业务处理失败返回 5xx，驱动渠道重试。
 *
 * @author : Tomatos
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders/payment")
public class PaymentController {

    private final PaymentApp paymentApp;

    /**
     * 创建支付单。
     *
     * @param orderNo 父订单号
     * @return 支付单信息
     */
    @PostMapping("/{orderNo}")
    public Result<PaymentCreateResp> create(@PathVariable("orderNo") String orderNo) {
        Long userId = currentUserId();
        return Result.success(paymentApp.create(orderNo, userId));
    }

    /**
     * 主动查询支付状态。
     *
     * @param orderNo 父订单号
     * @return 支付状态
     */
    @GetMapping("/{orderNo}")
    public Result<PaymentStatusResp> query(@PathVariable("orderNo") String orderNo) {
        Long userId = currentUserId();
        return Result.success(paymentApp.query(orderNo, userId));
    }

    /**
     * 查询订单是否已支付（前端轮询兼容入口）。
     *
     * @param orderNo 父订单号
     * @return true 表示已支付
     */
    @GetMapping("/status")
    public Result<Boolean> status(@RequestParam("orderNo") String orderNo) {
        Long userId = currentUserId();
        return Result.success(paymentApp.isPaid(orderNo, userId));
    }

    /**
     * 微信支付异步回调。
     *
     * @param body 原始回调报文
     * @param headers HTTP 头
     * @return 微信支付风格应答
     */
    @PostMapping("/notify/wechat")
    public ResponseEntity<WechatNotifyResp> notify(
        @RequestBody String body,
        @RequestHeader HttpHeaders headers
    ) {
        try {
            paymentApp.handleNotify(headers, body);
            return ResponseEntity.ok(WechatNotifyResp.success());
        } catch (BizException e) {
            log.warn("处理微信支付回调失败: {}", e.getMessage());
            HttpStatus status = e.getBizError() == BizErrorEnum.PAYMENT_CALLBACK_SIGNATURE_INVALID
                    ? HttpStatus.UNAUTHORIZED
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(WechatNotifyResp.fail(e.getMessage()));
        }
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 用户 ID
     */
    private static Long currentUserId() {
        return Asserts.requireNonNull(UserContextHolder.getUserId(), BizErrorEnum.USER_NOT_AUTHENTICATED);
    }
}
