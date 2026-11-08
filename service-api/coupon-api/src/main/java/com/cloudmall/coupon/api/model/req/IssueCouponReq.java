package com.cloudmall.coupon.api.model.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内部发券请求（积分兑换等场景）。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueCouponReq {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "优惠券模板ID不能为空")
    private Long couponId;

    /** 幂等键，同一用户同一键只发放一次 */
    private String idempotencyKey;
}
