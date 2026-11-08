package com.cloudmall.payment.api.model.resp;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模拟支付下单响应。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockPaymentCreateResp {

    /** 商户订单号 */
    private String outTradeNo;

    /** 模拟微信支付订单号 */
    private String transactionId;

    /** JSAPI 预支付会话标识 */
    private String prepayId;

    /** Native 支付二维码链接 */
    private String codeUrl;

    /** 模拟收银台地址 */
    private String cashierUrl;

    /** 交易状态 */
    private String tradeState;

    /** 交易状态描述 */
    private String tradeStateDesc;

    /** 订单总金额（分） */
    private Integer amountTotal;

    /** 支付截止时间 */
    private LocalDateTime expireTime;
}
