package com.cloudmall.order.model.resp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付状态响应。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResp {

    /** 业务订单号 */
    private String orderNo;

    /** 支付渠道商户订单号 */
    private String outTradeNo;

    /** 模拟微信支付订单号 */
    private String transactionId;

    /** 交易状态 */
    private String tradeState;

    /** 交易状态描述 */
    private String tradeStateDesc;

    /** 是否支付成功 */
    private boolean paid;

    /** 应付金额（元） */
    private BigDecimal amount;

    /** 支付完成时间 */
    private LocalDateTime successTime;

    /** 支付截止时间 */
    private LocalDateTime expireTime;

    /** 模拟收银台地址 */
    private String cashierUrl;
}
