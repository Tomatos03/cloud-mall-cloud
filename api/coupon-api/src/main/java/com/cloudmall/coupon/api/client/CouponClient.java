package com.cloudmall.coupon.api.client;

import com.cloudmall.common.entity.Result;
import com.cloudmall.coupon.api.response.CouponResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "coupon-service", path = "/coupon")
public interface CouponClient {
    @PostMapping("/verify")
    Result<CouponResp> verifyCoupon(@RequestParam("couponId") Long couponId,
                                         @RequestParam("userId") Long userId);
    @PostMapping("/use/{id}")
    Result<Void> markUsed(@PathVariable("id") Long id);
}
