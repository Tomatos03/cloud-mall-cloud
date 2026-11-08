package com.cloudmall.mockpayment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.mockpayment.enums.TradeTypeEnum;
import com.cloudmall.mockpayment.model.req.CreatePayReq;
import com.cloudmall.mockpayment.model.resp.MockPaymentDetailResp;
import com.cloudmall.mockpayment.model.resp.TransactionQueryResp;
import com.cloudmall.payment.api.model.req.MockPaymentCreateReq;
import com.cloudmall.payment.api.model.resp.MockPaymentCreateResp;
import com.cloudmall.payment.api.model.resp.MockPaymentQueryResp;

/**
 * 模拟微信支付服务。
 * <p>
 * 覆盖微信支付 API v3 的 JSAPI/Native 下单、订单查询、关闭订单，
 * 以及供业务服务使用的内部幂等接口和模拟控制台查询。
 *
 * @author : Tomatos
 */
public interface IWechatPayService {

    /**
     * 微信支付协议下单。
     *
     * @param req 下单请求
     * @param tradeType 交易类型
     * @return 预支付信息
     */
    MockPaymentCreateResp create(CreatePayReq req, TradeTypeEnum tradeType);

    /**
     * 内部接口下单。
     *
     * @param req 下单请求
     * @return 预支付信息
     */
    MockPaymentCreateResp createInternal(MockPaymentCreateReq req);

    /**
     * 按商户订单号查询订单。
     *
     * @param outTradeNo 商户订单号
     * @param mchid 商户号
     * @return 微信支付查询响应
     */
    TransactionQueryResp queryByOutTradeNo(String outTradeNo, String mchid);

    /**
     * 按模拟微信支付订单号查询订单。
     *
     * @param transactionId 模拟微信支付订单号
     * @param mchid 商户号
     * @return 微信支付查询响应
     */
    TransactionQueryResp queryByTransactionId(String transactionId, String mchid);

    /**
     * 内部接口查询订单。
     *
     * @param outTradeNo 商户订单号
     * @return 内部查询响应
     */
    MockPaymentQueryResp queryInternal(String outTradeNo);

    /**
     * 微信支付协议关闭订单。
     *
     * @param outTradeNo 商户订单号
     * @param mchid 商户号
     */
    void close(String outTradeNo, String mchid);

    /**
     * 内部接口关闭订单。
     *
     * @param outTradeNo 商户订单号
     */
    void closeInternal(String outTradeNo);

    /**
     * 模拟支付成功。
     *
     * @param outTradeNo 商户订单号
     * @return 支付单详情
     */
    MockPaymentDetailResp markPaid(String outTradeNo);

    /**
     * 查询支付单详情。
     *
     * @param outTradeNo 商户订单号
     * @return 支付单详情
     */
    MockPaymentDetailResp detail(String outTradeNo);

    /**
     * 分页查询模拟支付单。
     *
     * @param current 当前页
     * @param size 每页条数
     * @param tradeState 交易状态，可为空
     * @return 支付单分页
     */
    IPage<MockPaymentDetailResp> page(long current, long size, String tradeState);
}
