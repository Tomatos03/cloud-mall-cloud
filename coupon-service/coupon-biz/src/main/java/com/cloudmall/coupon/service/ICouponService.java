package com.cloudmall.coupon.service;

import com.cloudmall.coupon.api.request.CouponClaimRequest;
import com.cloudmall.coupon.api.response.CouponResponse;

import java.util.List;

public interface ICouponService {
    List<CouponResponse> listAvailable();

    CouponResponse getById(Long id);

    Boolean claim(CouponClaimRequest request);

    CouponResponse verifyCoupon(Long couponId, Long userId);

    void markUsed(Long id);
}
