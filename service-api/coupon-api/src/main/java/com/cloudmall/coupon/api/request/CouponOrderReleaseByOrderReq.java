package com.cloudmall.coupon.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 按订单释放用户券请求
 * <p>
 * 内部接口，由订单服务在取消/超时/补偿场景调用：
 * 释放该订单全部已锁定（LOCKED）的用户券，幂等。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponOrderReleaseByOrderReq {

    /** 用户 ID（内部接口显式传入，异步场景无用户上下文） */
    @NotNull
    private Long userId;

    /** 订单号（幂等键） */
    @NotBlank
    private String orderNo;
}
