package com.cloudmall.coupon.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClaimReq {
    @NotNull private Long couponId;
    @NotNull private Long userId;
}
