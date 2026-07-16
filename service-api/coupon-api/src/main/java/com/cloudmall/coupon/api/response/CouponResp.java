package com.cloudmall.coupon.api.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CouponResp {
    private Long id;
    private String name;
    private String type;        // FULL_REDUCTION, DISCOUNT, CASH
    private BigDecimal threshold;
    private BigDecimal discount;
    private LocalDateTime expireTime;
    private Boolean claimed;    // 当前用户是否已领取
}