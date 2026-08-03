package com.cloudmall.coupon.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.coupon.api.request.CouponOrderCommitReq;
import com.cloudmall.coupon.api.request.CouponOrderLockReq;
import com.cloudmall.coupon.api.request.CouponOrderQuoteReq;
import com.cloudmall.coupon.api.request.CouponOrderReleaseReq;
import com.cloudmall.coupon.api.request.CouponOrderRevertReq;
import com.cloudmall.coupon.api.response.CouponOrderCalcResp;
import com.cloudmall.coupon.service.ICouponOrderService;

/**
 * 优惠券订单协同内部接口
 * <p>
 * 仅供 order-service 通过 Feign 调用，不加入网关对外路由。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/coupon-orders")
public class CouponOrderController {

    private final ICouponOrderService couponOrderService;

    /**
     * 无副作用试算。
     *
     * @param req 试算请求
     * @return 优惠快照
     */
    @PostMapping("/quote")
    public Result<CouponOrderCalcResp> quote(
            @RequestBody
            @Valid
            CouponOrderQuoteReq req
    ) {
        return Result.success(couponOrderService.quote(req));
    }

    /**
     * 锁定用户券。
     *
     * @param req 锁定请求
     * @return 优惠快照
     */
    @PostMapping("/lock")
    public Result<CouponOrderCalcResp> lock(
            @RequestBody
            @Valid
            CouponOrderLockReq req
    ) {
        return Result.success(couponOrderService.lock(req));
    }

    /**
     * 支付成功核销。
     *
     * @param req 核销请求
     * @return 处理结果
     */
    @PostMapping("/commit")
    public Result<Void> commit(
            @RequestBody
            @Valid
            CouponOrderCommitReq req
    ) {
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
    public Result<Void> release(
            @RequestBody
            @Valid
            CouponOrderReleaseReq req
    ) {
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
    public Result<Void> revert(
            @RequestBody
            @Valid
            CouponOrderRevertReq req
    ) {
        couponOrderService.revert(req);
        return Result.success();
    }
}
