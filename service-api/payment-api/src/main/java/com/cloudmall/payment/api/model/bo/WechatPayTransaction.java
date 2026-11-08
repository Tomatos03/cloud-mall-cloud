package com.cloudmall.payment.api.model.bo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * 微信支付交易通知解密后的业务报文。
 * <p>
 * 对应回调 resource.ciphertext 解密后的 transaction JSON，
 * 字段与微信支付 API v3 查询订单响应保持一致。
 *
 * @param appid 公众号 APPID
 * @param mchid 商户号
 * @param outTradeNo 商户订单号
 * @param transactionId 微信支付订单号
 * @param tradeType 交易类型
 * @param tradeState 交易状态
 * @param tradeStateDesc 交易状态描述
 * @param bankType 付款银行
 * @param attach 附加数据
 * @param successTime 支付完成时间（RFC3339）
 * @param payer 支付者
 * @param amount 订单金额
 * @author : Tomatos
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record WechatPayTransaction(
    String appid,
    String mchid,
    String outTradeNo,
    String transactionId,
    String tradeType,
    String tradeState,
    String tradeStateDesc,
    String bankType,
    String attach,
    String successTime,
    Payer payer,
    Amount amount
) {

    /**
     * 支付者信息。
     *
     * @param openid 用户在公众号下的唯一标识
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Payer(String openid) {
    }

    /**
     * 订单金额信息。
     *
     * @param total 订单总金额（分）
     * @param payerTotal 用户实际支付金额（分）
     * @param currency 币种
     * @param payerCurrency 支付币种
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Amount(Integer total, Integer payerTotal, String currency, String payerCurrency) {
    }
}
