package com.cloudmall.coupon.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优惠券核销请求
 * <p>
 * 内部接口，由订单服务在支付成功事件中调用：LOCKED → USED。
 * 幂等键为订单号。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponOrderCommitReq {

    /** 用户 ID */
    @NotNull
    private Long userId;

    /** 订单号（幂等键） */
    @NotBlank
    private String orderNo;

    /** 用户券 ID */
    @NotNull
    private Long userCouponId;
}
