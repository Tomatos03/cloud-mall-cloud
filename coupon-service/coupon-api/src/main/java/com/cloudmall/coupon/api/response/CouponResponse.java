package com.cloudmall.coupon.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponResponse {
    private Long id;
    private String name;
    private String type;        // FULL_REDUCTION, DISCOUNT, CASH
    private BigDecimal threshold;
    private BigDecimal discount;
    private LocalDateTime expireTime;
    private Boolean claimed;    // 当前用户是否已领取
}
