package com.cloudmall.coupon.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.entity.Result;
import com.cloudmall.coupon.api.request.ClaimReq;
import com.cloudmall.coupon.api.response.CouponResp;
import com.cloudmall.coupon.service.ICouponService;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final ICouponService couponService;

    @GetMapping("/list")
    public Result<List<CouponResp>> list() {
        return Result.success(couponService.listAvailable());
    }

    @GetMapping("/{id}")
    public Result<CouponResp> getById(@PathVariable Long id) {
        return Result.success(couponService.getById(id));
    }

    @PostMapping("/claim")
    public Result<Boolean> claim(@RequestBody @Valid ClaimReq request) {
        return Result.success(couponService.claim(request));
    }

    @GetMapping("/verify")
    public Result<CouponResp> verify(@RequestParam Long couponId, @RequestParam Long userId) {
        return Result.success(couponService.verifyCoupon(couponId, userId));
    }

    @PutMapping("/use/{id}")
    public Result<Void> markUsed(@PathVariable Long id) {
        couponService.markUsed(id);
        return Result.success(null);
    }
}
