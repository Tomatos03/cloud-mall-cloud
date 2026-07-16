package com.cloudmall.coupon.controller;

import com.cloudmall.common.entity.Result;
import com.cloudmall.coupon.api.request.ClaimReq;
import com.cloudmall.coupon.api.response.CouponResp;
import com.cloudmall.coupon.service.ICouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coupon")
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

    @PostMapping("/verify")
    public Result<CouponResp> verify(@RequestParam Long couponId, @RequestParam Long userId) {
        return Result.success(couponService.verifyCoupon(couponId, userId));
    }

    @PostMapping("/use/{id}")
    public Result<Void> markUsed(@PathVariable Long id) {
        couponService.markUsed(id);
        return Result.success(null);
    }
}
