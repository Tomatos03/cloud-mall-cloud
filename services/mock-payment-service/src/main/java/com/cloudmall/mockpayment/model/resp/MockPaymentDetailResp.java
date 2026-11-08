package com.cloudmall.mockpayment.model.resp;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模拟支付单详情（模拟控制台使用）。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockPaymentDetailResp {

    /** 商户订单号 */
    private String outTradeNo;

    /** 模拟微信支付订单号 */
    private String transactionId;

    /** 商品描述 */
    private String description;

    /** 订单总金额（分） */
    private Integer amountTotal;

    /** 币种 */
    private String currency;

    /** 交易类型 */
    private String tradeType;

    /** 交易状态 */
    private String tradeState;

    /** 交易状态描述 */
    private String tradeStateDesc;

    /** 支付者标识 */
    private String payerOpenid;

    /** 回调地址 */
    private String notifyUrl;

    /** 预支付会话标识 */
    private String prepayId;

    /** Native 二维码链接 */
    private String codeUrl;

    /** 模拟收银台地址 */
    private String cashierUrl;

    /** 支付完成时间 */
    private LocalDateTime successTime;

    /** 支付截止时间 */
    private LocalDateTime expireTime;

    /** 创建时间 */
    private LocalDateTime createTime;
}
