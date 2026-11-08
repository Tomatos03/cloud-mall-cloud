package com.cloudmall.mockpayment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.mockpayment.service.IWechatPayService;
import com.cloudmall.payment.api.model.req.MockPaymentCreateReq;
import com.cloudmall.payment.api.model.resp.MockPaymentCreateResp;
import com.cloudmall.payment.api.model.resp.MockPaymentQueryResp;

/**
 * 模拟支付内部接口。
 * <p>
 * 内部端点统一使用 /internal/mock-payment，返回项目统一 Result，
 * 供 order-service 等业务服务通过 Feign 调用，不加入网关对外路由。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/mock-payment")
public class InternalMockPaymentController {

    private final IWechatPayService wechatPayService;

    /**
     * 创建支付单（同一商户订单号幂等）。
     *
     * @param req 下单请求
     * @return 预支付信息
     */
    @PostMapping("/transactions")
    public Result<MockPaymentCreateResp> create(@RequestBody @Valid MockPaymentCreateReq req) {
        return Result.success(wechatPayService.createInternal(req));
    }

    /**
     * 查询支付单。
     *
     * @param outTradeNo 商户订单号
     * @return 支付单状态
     */
    @GetMapping("/transactions/{outTradeNo}")
    public Result<MockPaymentQueryResp> query(@PathVariable("outTradeNo") String outTradeNo) {
        return Result.success(wechatPayService.queryInternal(outTradeNo));
    }

    /**
     * 关闭支付单。
     *
     * @param outTradeNo 商户订单号
     * @return 处理结果
     */
    @PostMapping("/transactions/{outTradeNo}/close")
    public Result<Void> close(@PathVariable("outTradeNo") String outTradeNo) {
        wechatPayService.closeInternal(outTradeNo);
        return Result.success();
    }
}
