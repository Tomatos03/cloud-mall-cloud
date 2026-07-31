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

import com.cloudmall.common.model.Result;
import com.cloudmall.coupon.api.request.ClaimReq;
import com.cloudmall.coupon.api.response.CouponResp;
import com.cloudmall.coupon.service.ICouponService;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final ICouponService couponService;

    /**
     * 查询数据列表。
     *
     * @return 处理结果
     */
    @GetMapping
    public Result<List<CouponResp>> list() {
        return Result.success(couponService.listAvailable());
    }

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     */
    @GetMapping("/{id}")
    public Result<CouponResp> getById(@PathVariable Long id) {
        return Result.success(couponService.getById(id));
    }

    /**
     * 领取优惠券。
     *
     * @param request 请求参数
     * @return 处理结果
     */
    @PostMapping("/claim")
    public Result<Boolean> claim(@RequestBody @Valid ClaimReq request) {
        return Result.success(couponService.claim(request));
    }

    /**
     * 校验相关数据。
     *
     * @param couponId 优惠券标识
     * @param userId 用户标识
     * @return 处理结果
     */
    @GetMapping("/verify")
    public Result<CouponResp> verify(@RequestParam Long couponId, @RequestParam Long userId) {
        return Result.success(couponService.verifyCoupon(couponId, userId));
    }

    /**
     * 标记优惠券为已使用。
     *
     * @param id 对象标识
     * @return 处理结果
     */
    @PutMapping("/use/{id}")
    public Result<Void> markUsed(@PathVariable Long id) {
        couponService.markUsed(id);
        return Result.success(null);
    }
}
