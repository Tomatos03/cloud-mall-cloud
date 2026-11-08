package com.cloudmall.mockpayment.model.resp;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模拟支付回调投递记录响应。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockPaymentNotifyResp {

    /** 通知记录 ID */
    private Long id;

    /** 商户订单号 */
    private String outTradeNo;

    /** 回调事件类型 */
    private String eventType;

    /** 回调地址 */
    private String notifyUrl;

    /** 投递状态 */
    private String status;

    /** 已重试次数 */
    private Integer retryCount;

    /** 下次重试时间 */
    private LocalDateTime nextRetryTime;

    /** 最近一次失败原因 */
    private String lastError;

    /** 最近一次应答报文 */
    private String responseBody;

    /** 创建时间 */
    private LocalDateTime createTime;
}
