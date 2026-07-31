package com.cloudmall.coupon.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 领取优惠券请求
 * <p>
 * 用户端不接收 userId，统一从请求上下文获取当前用户。
 *
 * @author : Tomatos
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ClaimReq {

    /** 优惠券模板 ID */
    @NotNull
    private Long couponId;
}
