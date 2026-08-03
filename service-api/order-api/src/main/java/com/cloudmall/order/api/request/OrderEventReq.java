package com.cloudmall.order.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单事件请求
 * <p>
 * 内部接口，模拟支付回调/超时/取消/退款触发订单生命周期事件。
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
}
