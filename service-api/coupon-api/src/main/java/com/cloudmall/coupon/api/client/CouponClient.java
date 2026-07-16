package com.cloudmall.coupon.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cloudmall.common.entity.Result;
import com.cloudmall.coupon.api.response.CouponResp;

@FeignClient(name = "coupon-service", path = "/coupons")
public interface CouponClient {
    @GetMapping("/verify")
    Result<CouponResp> verifyCoupon(@RequestParam("couponId") Long couponId,
                                         @RequestParam("userId") Long userId);
    @PutMapping("/use/{id}")
    Result<Void> markUsed(@PathVariable("id") Long id);
}