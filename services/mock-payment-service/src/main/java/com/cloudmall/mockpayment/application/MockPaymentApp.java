package com.cloudmall.mockpayment.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.mockpayment.model.resp.MockPaymentDetailResp;
import com.cloudmall.mockpayment.service.IPaymentNotifyService;
import com.cloudmall.mockpayment.service.IWechatPayService;

/**
 * 模拟支付应用服务。
 * <p>
 * 编排“支付成功状态落库 + 异步回调投递”，控制器只负责协议参数接收。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class MockPaymentApp {

    private final IWechatPayService wechatPayService;
    private final IPaymentNotifyService paymentNotifyService;

    /**
     * 模拟用户支付成功并触发回调。
     *
     * @param outTradeNo 商户订单号
     * @return 支付单详情
     */
    public MockPaymentDetailResp pay(String outTradeNo) {
        MockPaymentDetailResp detail = wechatPayService.markPaid(outTradeNo);
        paymentNotifyService.notifyPaymentSuccess(outTradeNo);
        return detail;
    }

    /**
     * 手动重发回调。
     *
     * @param outTradeNo 商户订单号
     */
    public void resend(String outTradeNo) {
        paymentNotifyService.resend(outTradeNo);
    }
}
