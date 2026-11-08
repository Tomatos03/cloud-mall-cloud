package com.cloudmall.mockpayment.model.resp;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信支付查询订单响应。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransactionQueryResp {

    /** 公众号 APPID */
    private String appid;

    /** 商户号 */
    private String mchid;

    /** 商户订单号 */
    private String outTradeNo;

    /** 模拟微信支付订单号 */
    private String transactionId;

    /** 交易类型 */
    private String tradeType;

    /** 交易状态 */
    private String tradeState;

    /** 交易状态描述 */
    private String tradeStateDesc;

    /** 付款银行（模拟固定值） */
    private String bankType;

    /** 附加数据 */
    private String attach;

    /** 支付完成时间（RFC3339） */
    private String successTime;

    /** 支付者信息 */
    private PayerResp payer;

    /** 订单金额 */
    private AmountResp amount;
}
