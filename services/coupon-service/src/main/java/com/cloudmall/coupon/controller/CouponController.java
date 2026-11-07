package com.cloudmall.coupon.controller;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.CursorResult;
import com.cloudmall.common.model.Result;
import com.cloudmall.coupon.api.model.resp.CouponResp;
import com.cloudmall.coupon.api.model.resp.UserCouponResp;
import com.cloudmall.coupon.api.enums.IssueChannelEnum;
import com.cloudmall.coupon.model.req.CouponPageReq;
import com.cloudmall.coupon.model.req.UserCouponCursorReq;
import com.cloudmall.coupon.model.req.UserCouponPageReq;
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
@RequiredArgsConstructor
@RequestMapping("/coupons")
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
     * 分页查询公开券中心（仅公开领取模板）。
     *
     * @param req 分页请求（name 为空表示全部）
     * @return 模板分页
     */
    @GetMapping("/page")
    public Result<IPage<CouponResp>> page(CouponPageReq req) {
        req.setIssueChannel(IssueChannelEnum.PUBLIC_CLAIM);
        return Result.success(userCouponService.pagePublic(UserContextHolder.getUserId(), req));
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
     * @param req 分页请求（status 为空表示全部）
     * @return 分页结果
     */
    @GetMapping("/me")
    public Result<IPage<UserCouponResp>> pageMine(UserCouponPageReq req) {
        IPage<UserCouponResp> page = userCouponService.pageMine(UserContextHolder.getUserId(), req);
        return Result.success(page);
    }

    /**
     * 游标分页查询我的优惠券。
     *
     * @param req 游标请求（status 为空表示全部状态）
     * @return 游标分页结果
     */
    @PostMapping("/me/cursor")
    public Result<CursorResult<UserCouponResp>> pageMineCursor(@RequestBody @Valid UserCouponCursorReq req) {
        return Result.success(userCouponService.pageMineByCursor(UserContextHolder.getUserId(), req));
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
