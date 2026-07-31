package com.cloudmall.coupon.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.Result;
import com.cloudmall.coupon.api.response.CouponResp;
import com.cloudmall.coupon.api.response.UserCouponResp;
import com.cloudmall.coupon.enums.UserCouponStatusEnum;
import com.cloudmall.coupon.service.IUserCouponService;

/**
 * 优惠券用户端接口
 * <p>
 * 公开券中心匿名可浏览，登录用户可领取并查询我的优惠券。
 * 用户端不接收 userId，统一从请求上下文获取当前用户。
 *
 * @author : Tomatos
 */
@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final IUserCouponService userCouponService;

    /**
     * 查询公开券中心列表（仅公开领取模板）。
     *
     * @return 模板列表
     */
    @GetMapping
    public Result<List<CouponResp>> list() {
        return Result.success(userCouponService.listPublicCoupons(UserContextHolder.getUserId()));
    }

    /**
     * 查看公开模板详情。
     *
     * @param id 模板 ID
     * @return 模板详情
     */
    @GetMapping("/{id}")
    public Result<CouponResp> getById(@PathVariable Long id) {
        return Result.success(userCouponService.getPublicCoupon(id, UserContextHolder.getUserId()));
    }

    /**
     * 当前用户领取优惠券，重复领取幂等返回原用户券。
     *
     * @param id 模板 ID
     * @return 用户券
     */
    @PostMapping("/{id}/claim")
    public Result<UserCouponResp> claim(@PathVariable Long id) {
        return Result.success(userCouponService.claimPublic(id, UserContextHolder.getUserId()));
    }

    /**
     * 分页查询我的优惠券。
     *
     * @param current 页码
     * @param size    每页条数
     * @param status  状态过滤（AVAILABLE/LOCKED/USED/EXPIRED/REVOKED，默认查询全部）
     * @return 分页结果
     */
    @GetMapping("/me")
    public Result<Page<UserCouponResp>> pageMine(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) UserCouponStatusEnum status
    ) {
        Page<UserCouponResp> page = userCouponService.pageMine(
                UserContextHolder.getUserId(), status, current, size
        );
        return Result.success(page);
    }

    /**
     * 查看我的单张用户券详情。
     *
     * @param id 用户券 ID
     * @return 用户券
     */
    @GetMapping("/me/{id}")
    public Result<UserCouponResp> getMineById(@PathVariable Long id) {
        return Result.success(userCouponService.getMine(UserContextHolder.getUserId(), id));
    }
}
