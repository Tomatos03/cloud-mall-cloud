package com.cloudmall.coupon.controller;

import java.util.List;

import cn.hutool.core.util.StrUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.Result;
import com.cloudmall.coupon.api.model.req.AutoIssueRegisterReq;
import com.cloudmall.coupon.api.model.req.CouponOrderCommitByOrderReq;
import com.cloudmall.coupon.api.model.req.CouponOrderCommitReq;
import com.cloudmall.coupon.api.model.req.CouponOrderLockReq;
import com.cloudmall.coupon.api.model.req.CouponOrderQuoteReq;
import com.cloudmall.coupon.api.model.req.CouponOrderReleaseByOrderReq;
import com.cloudmall.coupon.api.model.req.CouponOrderReleaseReq;
import com.cloudmall.coupon.api.model.req.CouponOrderRevertByOrderReq;
import com.cloudmall.coupon.api.model.req.CouponOrderRevertReq;
import com.cloudmall.coupon.api.model.req.IssueCouponReq;
import com.cloudmall.coupon.api.model.resp.CouponOrderCalcResp;
import com.cloudmall.coupon.api.model.resp.UserCouponResp;
import com.cloudmall.coupon.enums.UserCouponStatusEnum;
import com.cloudmall.coupon.model.req.UserCouponPageReq;
import com.cloudmall.coupon.service.ICouponOrderService;
import com.cloudmall.coupon.service.IUserCouponService;

/**
 * 优惠券内部接口
 * <p>
 * 内部服务端点统一使用 /internal/coupon，仅供服务间 Feign 调用，不加入网关对外路由。
 * 用户券协同（自动发放）与订单协同（试算/锁定/核销/释放/回退）统一收敛于此。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/coupon")
public class InternalCouponController {

    private final ICouponOrderService couponOrderService;
    private final IUserCouponService userCouponService;

    /**
     * 注册自动发放新人券（按事件 ID 幂等）。
     *
     * @param req 发券请求
     * @return 处理结果
     */
    @PostMapping("/auto-issue-register")
    public Result<Void> autoIssueForRegister(@RequestBody @Valid AutoIssueRegisterReq req) {
        userCouponService.autoIssueForRegister(req.getUserId(), req.getEventId());
        return Result.success();
    }

    /**
     * 内部发券（积分兑换等场景，按幂等键防重）。
     *
     * @param req 发券请求
     * @return 用户券
     */
    @PostMapping("/issue")
    public Result<UserCouponResp> issue(@RequestBody @Valid IssueCouponReq req) {
        return Result.success(
                userCouponService.issueForExchange(
                        req.getUserId(),
                        req.getCouponId(),
                        req.getIdempotencyKey()
                )
        );
    }

    /**
     * 无副作用试算。
     *
     * @param req 试算请求
     * @return 优惠快照
     */
    @PostMapping("/quote")
    public Result<CouponOrderCalcResp> quote(@RequestBody @Valid CouponOrderQuoteReq req) {
        return Result.success(couponOrderService.quote(req));
    }

    /**
     * 锁定用户券。
     *
     * @param req 锁定请求
     * @return 优惠快照
     */
    @PostMapping("/lock")
    public Result<CouponOrderCalcResp> lock(@RequestBody @Valid CouponOrderLockReq req) {
        return Result.success(couponOrderService.lock(req));
    }

    /**
     * 支付成功核销。
     *
     * @param req 核销请求
     * @return 处理结果
     */
    @PostMapping("/commit")
    public Result<Void> commit(@RequestBody @Valid CouponOrderCommitReq req) {
        couponOrderService.commit(req);
        return Result.success();
    }

    /**
     * 取消/超时释放。
     *
     * @param req 释放请求
     * @return 处理结果
     */
    @PostMapping("/release")
    public Result<Void> release(@RequestBody @Valid CouponOrderReleaseReq req) {
        couponOrderService.release(req);
        return Result.success();
    }

    /**
     * 全额退款回退。
     *
     * @param req 回退请求
     * @return 处理结果
     */
    @PostMapping("/revert")
    public Result<Void> revert(@RequestBody @Valid CouponOrderRevertReq req) {
        couponOrderService.revert(req);
        return Result.success();
    }

    /**
     * 按订单释放全部已锁定用户券。
     *
     * @param req 释放请求
     * @return 处理结果
     */
    @PostMapping("/release-by-order")
    public Result<Void> releaseByOrder(@RequestBody @Valid CouponOrderReleaseByOrderReq req) {
        couponOrderService.releaseByOrder(req);
        return Result.success();
    }

    /**
     * 按订单核销全部已锁定用户券。
     *
     * @param req 核销请求
     * @return 处理结果
     */
    @PostMapping("/commit-by-order")
    public Result<Void> commitByOrder(@RequestBody @Valid CouponOrderCommitByOrderReq req) {
        couponOrderService.commitByOrder(req);
        return Result.success();
    }

    /**
     * 按订单回退全部已核销用户券。
     *
     * @param req 回退请求
     * @return 处理结果
     */
    @PostMapping("/revert-by-order")
    public Result<Void> revertByOrder(@RequestBody @Valid CouponOrderRevertByOrderReq req) {
        couponOrderService.revertByOrder(req);
        return Result.success();
    }

    /**
     * 查询当前用户的优惠券列表（AI 客服使用）。
     *
     * @param current 当前页码
     * @param size 每页条数
     * @param status 用户券状态，可为空
     * @return 用户券列表
     */
    @GetMapping("/user-coupons")
    public Result<List<UserCouponResp>> listUserCoupons(
        @RequestParam(defaultValue = "1") long current,
        @RequestParam(defaultValue = "5") long size,
        @RequestParam(required = false) String status
    ) {
        UserCouponPageReq req = new UserCouponPageReq(current, size);
        if (StrUtil.isNotBlank(status)) {
            req.setStatus(UserCouponStatusEnum.fromCode(status));
        }
        return Result.success(userCouponService.pageMine(UserContextHolder.getUserId(), req).getRecords());
    }
}
