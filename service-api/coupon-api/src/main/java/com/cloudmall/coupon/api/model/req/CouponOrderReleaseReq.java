package com.cloudmall.coupon.api.model.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优惠券释放请求
 * <p>
 * 内部接口，由订单服务在取消/超时事件中调用：LOCKED → AVAILABLE/EXPIRED。
 * 幂等键为订单号。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponOrderReleaseReq {

    /** 用户 ID */
    @NotNull
    private Long userId;

    /** 订单号（幂等键） */
    @NotBlank
    private String orderNo;

    /** 用户券 ID */
    @NotNull
    private Long userCouponId;

    /** 释放原因（可空，默认"订单取消/超时释放"） */
    private String reason;
}
