package com.cloudmall.mockpayment.service;

import java.util.List;

import com.cloudmall.mockpayment.model.resp.MockPaymentNotifyResp;

/**
 * 模拟支付回调通知服务。
 * <p>
 * 负责构造微信支付风格的加密回调报文、投递到业务 notify_url，
 * 并在失败时按指数退避重试。
 *
 * @author : Tomatos
 */
public interface IPaymentNotifyService {

    /**
     * 支付成功后创建并投递回调通知（同一订单只保留一条有效回调记录）。
     *
     * @param outTradeNo 商户订单号
     */
    void notifyPaymentSuccess(String outTradeNo);

    /**
     * 投递指定通知记录。
     *
     * @param notifyId 通知记录 ID
     */
    void send(Long notifyId);

    /**
     * 手动重发指定订单的最近一次回调。
     *
     * @param outTradeNo 商户订单号
     */
    void resend(String outTradeNo);

    /**
     * 查询订单下的回调记录。
     *
     * @param outTradeNo 商户订单号
     * @return 回调记录
     */
    List<MockPaymentNotifyResp> listByOutTradeNo(String outTradeNo);

    /**
     * 重试到期的失败通知。
     */
    void retryDue();
}
