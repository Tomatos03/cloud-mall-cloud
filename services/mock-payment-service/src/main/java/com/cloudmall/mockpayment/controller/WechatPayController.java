package com.cloudmall.mockpayment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.mockpayment.enums.TradeTypeEnum;
import com.cloudmall.mockpayment.model.req.CloseOrderReq;
import com.cloudmall.mockpayment.model.req.CreatePayReq;
import com.cloudmall.mockpayment.model.resp.NativePayResp;
import com.cloudmall.mockpayment.model.resp.PrepayResp;
import com.cloudmall.mockpayment.model.resp.TransactionQueryResp;
import com.cloudmall.mockpayment.service.IWechatPayService;
import com.cloudmall.payment.api.model.resp.MockPaymentCreateResp;

/**
 * 微信支付 API v3 模拟接口。
 * <p>
 * 路径、请求字段与响应结构对齐微信支付官方文档，便于业务侧无感替换为真实渠道；
 * 响应错误由 {@link com.cloudmall.mockpayment.exception.WechatPayExceptionHandler} 转换为微信风格。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v3/pay/transactions")
public class WechatPayController {

    private final IWechatPayService wechatPayService;

    /**
     * JSAPI 下单。
     *
     * @param req 下单请求
     * @return 预支付会话标识
     */
    @PostMapping("/jsapi")
    public PrepayResp jsapi(@RequestBody @Valid CreatePayReq req) {
        MockPaymentCreateResp resp = wechatPayService.create(req, TradeTypeEnum.JSAPI);
        return PrepayResp.builder()
                         .prepayId(resp.getPrepayId())
                         .build();
    }

    /**
     * Native 下单。
     *
     * @param req 下单请求
     * @return 二维码链接
     */
    @PostMapping("/native")
    public NativePayResp nativePay(@RequestBody @Valid CreatePayReq req) {
        MockPaymentCreateResp resp = wechatPayService.create(req, TradeTypeEnum.NATIVE);
        return NativePayResp.builder()
                            .codeUrl(resp.getCodeUrl())
                            .build();
    }

    /**
     * 商户订单号查询订单。
     *
     * @param outTradeNo 商户订单号
     * @param mchid 商户号
     * @return 订单详情
     */
    @GetMapping("/out-trade-no/{outTradeNo}")
    public TransactionQueryResp queryByOutTradeNo(
        @PathVariable("outTradeNo") String outTradeNo,
        @RequestParam("mchid") String mchid
    ) {
        return wechatPayService.queryByOutTradeNo(outTradeNo, mchid);
    }

    /**
     * 微信支付订单号查询订单。
     *
     * @param transactionId 模拟微信支付订单号
     * @param mchid 商户号
     * @return 订单详情
     */
    @GetMapping("/id/{transactionId}")
    public TransactionQueryResp queryByTransactionId(
        @PathVariable("transactionId") String transactionId,
        @RequestParam("mchid") String mchid
    ) {
        return wechatPayService.queryByTransactionId(transactionId, mchid);
    }

    /**
     * 关闭订单。
     *
     * @param outTradeNo 商户订单号
     * @param req 关单请求
     * @return 204 无内容
     */
    @PostMapping("/out-trade-no/{outTradeNo}/close")
    public ResponseEntity<Void> close(
        @PathVariable("outTradeNo") String outTradeNo,
        @RequestBody @Valid CloseOrderReq req
    ) {
        wechatPayService.close(outTradeNo, req.getMchid());
        return ResponseEntity.noContent().build();
    }
}
