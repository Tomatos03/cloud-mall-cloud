package com.cloudmall.mockpayment.controller;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.mockpayment.application.MockPaymentApp;
import com.cloudmall.mockpayment.model.resp.MockPaymentConfigResp;
import com.cloudmall.mockpayment.model.resp.MockPaymentDetailResp;
import com.cloudmall.mockpayment.model.resp.MockPaymentNotifyResp;
import com.cloudmall.mockpayment.properties.MockPaymentProperties;
import com.cloudmall.mockpayment.service.IPaymentNotifyService;
import com.cloudmall.mockpayment.service.IWechatPayService;
import com.cloudmall.mockpayment.service.impl.WechatPaySignatureService;

/**
 * 模拟支付控制台接口。
 * <p>
 * 提供支付单浏览、模拟支付成功、关单和回调重发能力，方便本地联调；
 * 这些接口不属于微信支付协议，路径统一收敛于 /mock/payments。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/mock/payments")
public class MockPaymentController {

    private final IWechatPayService wechatPayService;
    private final IPaymentNotifyService paymentNotifyService;
    private final MockPaymentApp mockPaymentApp;
    private final MockPaymentProperties properties;
    private final WechatPaySignatureService signatureService;

    /**
     * 分页查询模拟支付单。
     *
     * @param current 当前页
     * @param size 每页条数
     * @param tradeState 交易状态，可为空
     * @return 支付单分页
     */
    @GetMapping
    public Result<IPage<MockPaymentDetailResp>> page(
        @RequestParam(defaultValue = "1") long current,
        @RequestParam(defaultValue = "10") long size,
        @RequestParam(required = false) String tradeState
    ) {
        return Result.success(wechatPayService.page(current, size, tradeState));
    }

    /**
     * 查询支付单详情。
     *
     * @param outTradeNo 商户订单号
     * @return 支付单详情
     */
    @GetMapping("/{outTradeNo}")
    public Result<MockPaymentDetailResp> detail(@PathVariable("outTradeNo") String outTradeNo) {
        return Result.success(wechatPayService.detail(outTradeNo));
    }

    /**
     * 模拟用户支付成功并触发回调。
     *
     * @param outTradeNo 商户订单号
     * @return 支付单详情
     */
    @PostMapping("/{outTradeNo}/pay")
    public Result<MockPaymentDetailResp> pay(@PathVariable("outTradeNo") String outTradeNo) {
        return Result.success(mockPaymentApp.pay(outTradeNo));
    }

    /**
     * 关闭支付单。
     *
     * @param outTradeNo 商户订单号
     * @return 处理结果
     */
    @PostMapping("/{outTradeNo}/close")
    public Result<Void> close(@PathVariable("outTradeNo") String outTradeNo) {
        wechatPayService.closeInternal(outTradeNo);
        return Result.success();
    }

    /**
     * 手动重发回调。
     *
     * @param outTradeNo 商户订单号
     * @return 处理结果
     */
    @PostMapping("/{outTradeNo}/notify")
    public Result<Void> resend(@PathVariable("outTradeNo") String outTradeNo) {
        mockPaymentApp.resend(outTradeNo);
        return Result.success();
    }

    /**
     * 查询支付单的回调记录。
     *
     * @param outTradeNo 商户订单号
     * @return 回调记录
     */
    @GetMapping("/{outTradeNo}/notifications")
    public Result<List<MockPaymentNotifyResp>> notifications(@PathVariable("outTradeNo") String outTradeNo) {
        return Result.success(paymentNotifyService.listByOutTradeNo(outTradeNo));
    }

    /**
     * 查询模拟支付公开配置（平台公钥等，便于业务侧验签）。
     *
     * @return 公开配置
     */
    @GetMapping("/config")
    public Result<MockPaymentConfigResp> config() {
        return Result.success(
                MockPaymentConfigResp.builder()
                                     .appid(properties.getAppid())
                                     .mchid(properties.getMchid())
                                     .publicBaseUrl(properties.getPublicBaseUrl())
                                     .platformSerialNo(properties.getPlatformSerialNo())
                                     .platformPublicKey(signatureService.platformPublicKeyPem())
                                     .verifyRequest(properties.getSignature().isVerifyRequest())
                                     .build()
        );
    }
}
