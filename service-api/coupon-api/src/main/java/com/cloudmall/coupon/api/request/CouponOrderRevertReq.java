package com.cloudmall.coupon.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优惠券回退请求
 * <p>
 * 内部接口，由订单服务在父订单全额退款事件中调用：USED → AVAILABLE/EXPIRED。
 * 幂等键为订单号 + 退款单号。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponOrderRevertReq {

    /** 用户 ID */
    @NotNull
    private Long userId;

    /** 父订单号（幂等键） */
    @NotBlank
    private String orderNo;

    /** 用户券 ID */
    @NotNull
    private Long userCouponId;

    /** 退款单 ID（幂等键） */
    @NotBlank
    private String refundId;

    /** 回退原因（可空，默认"全额退款回退"） */
    private String reason;
}
