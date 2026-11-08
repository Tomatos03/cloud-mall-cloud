package com.cloudmall.order.model.resp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建支付单响应。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateResp {

    /** 业务订单号 */
    private String orderNo;

    /** 支付渠道商户订单号 */
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

    /** 应付金额（元） */
    private BigDecimal amount;

    /** 支付截止时间 */
    private LocalDateTime expireTime;
}
