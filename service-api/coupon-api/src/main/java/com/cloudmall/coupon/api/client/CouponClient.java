package com.cloudmall.coupon.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cloudmall.common.model.Result;
import com.cloudmall.coupon.api.response.CouponResp;

@FeignClient(name = "coupon-service", contextId = "couponClient", path = "/coupons")
public interface CouponClient {

    /**
     * 校验相关数据。
     *
     * @param couponId 优惠券标识
     * @param userId 用户标识
     * @return 处理结果
     */
    @GetMapping("/verify")
    Result<CouponResp> verifyCoupon(@RequestParam("couponId") Long couponId,
                                         @RequestParam("userId") Long userId);

    /**
     * 标记优惠券为已使用。
     *
     * @param id 对象标识
     * @return 处理结果
     */
    @PutMapping("/use/{id}")
    Result<Void> markUsed(@PathVariable("id") Long id);
}