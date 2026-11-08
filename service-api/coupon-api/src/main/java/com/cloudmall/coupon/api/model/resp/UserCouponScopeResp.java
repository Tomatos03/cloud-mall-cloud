package com.cloudmall.coupon.api.model.resp;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户券适用范围响应。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponScopeResp {

    private Long userCouponId;

    private Long couponId;

    /** ALL / PRODUCT / CATEGORY */
    private String scopeType;

    private List<Long> productIds;

    private List<Long> categoryIds;

    private BigDecimal threshold;

    /** 当前用户券是否处于可用状态 */
    private Boolean usable;
}
