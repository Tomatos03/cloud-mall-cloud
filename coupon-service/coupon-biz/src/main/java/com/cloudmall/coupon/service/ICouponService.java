package com.cloudmall.coupon.service;

import com.cloudmall.coupon.api.request.ClaimReq;
import com.cloudmall.coupon.api.response.CouponResp;

import java.util.List;

public interface ICouponService {
    List<CouponResp> listAvailable();

    CouponResp getById(Long id);

    Boolean claim(ClaimReq request);

    CouponResp verifyCoupon(Long couponId, Long userId);

    void markUsed(Long id);
}
