package com.cloudmall.order.api.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单事件请求
 * <p>
 * 内部接口，模拟支付回调/超时/取消/退款触发订单生命周期事件。
 * 支付成功回调必须携带 payAmount，全额退款回调必须携带 refundAmount，
 * 金额由 order-service 与订单应付金额快照比对，不一致拒绝处理。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventReq {

    /** 父订单号 */
    @NotBlank
    private String orderNo;

    /** 退款单 ID（全额退款事件） */
    private String refundId;

    /** 第三方支付平台透传的支付金额（支付事件必填，金额对比前先做单位转换） */
    @DecimalMin("0.01")
    private BigDecimal payAmount;

    /** 第三方支付平台透传的退款金额（退款事件必填，金额对比前先做单位转换） */
    @DecimalMin("0.01")
    private BigDecimal refundAmount;
}
