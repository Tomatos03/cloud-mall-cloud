package com.cloudmall.coupon.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.coupon.api.request.AutoIssueRegisterReq;
import com.cloudmall.coupon.api.request.CouponOrderCommitByOrderReq;
import com.cloudmall.coupon.api.request.CouponOrderCommitReq;
import com.cloudmall.coupon.api.request.CouponOrderLockReq;
import com.cloudmall.coupon.api.request.CouponOrderQuoteReq;
import com.cloudmall.coupon.api.request.CouponOrderReleaseByOrderReq;
import com.cloudmall.coupon.api.request.CouponOrderReleaseReq;
import com.cloudmall.coupon.api.request.CouponOrderRevertByOrderReq;
import com.cloudmall.coupon.api.request.CouponOrderRevertReq;
import com.cloudmall.coupon.api.response.CouponOrderCalcResp;

/**
 * 优惠券内部 Feign 客户端
 * <p>
 * 内部接口统一收敛于 InternalCouponClient（/internal/coupon），
 * 覆盖用户券协同（自动发放）与订单协同（试算/锁定/核销/释放/回退），
 * 仅供服务间 Feign 调用，不加入网关对外路由。
 *
 * @author : Tomatos
 */
@FeignClient(name = "coupon-service", contextId = "internalCouponClient", path = "/internal/coupon")
public interface InternalCouponClient {

    /**
     * 注册自动发放新人券（按事件 ID 幂等）。
     *
     * @param req 发券请求
     */
    @PostMapping("/auto-issue-register")
    void autoIssueForRegister(
            @RequestBody
            AutoIssueRegisterReq req
    );

    /**
     * 无副作用试算。
     *
     * @param req 试算请求
     * @return 优惠快照
     */
    @PostMapping("/quote")
    CouponOrderCalcResp quote(
            @RequestBody
            CouponOrderQuoteReq req
    );

    /**
     * 重新计算并锁定用户券。
     *
     * @param req 锁定请求
     * @return 优惠快照（含实际使用的券快照）
     */
    @PostMapping("/lock")
    CouponOrderCalcResp lock(
            @RequestBody
            CouponOrderLockReq req
    );

    /**
     * 支付成功核销。
     *
     * @param req 核销请求
     */
    @PostMapping("/commit")
    void commit(
            @RequestBody
            CouponOrderCommitReq req
    );

    /**
     * 取消/超时释放。
     *
     * @param req 释放请求
     */
    @PostMapping("/release")
    void release(
            @RequestBody
            CouponOrderReleaseReq req
    );

    /**
     * 全额退款回退。
     *
     * @param req 回退请求
     */
    @PostMapping("/revert")
    void revert(
            @RequestBody
            CouponOrderRevertReq req
    );

    /**
     * 按订单释放全部已锁定用户券。
     *
     * @param req 释放请求
     */
    @PostMapping("/release-by-order")
    void releaseByOrder(
            @RequestBody
            CouponOrderReleaseByOrderReq req
    );

    /**
     * 按订单核销全部已锁定用户券。
     *
     * @param req 核销请求
     */
    @PostMapping("/commit-by-order")
    void commitByOrder(
            @RequestBody
            CouponOrderCommitByOrderReq req
    );

    /**
     * 按订单回退全部已核销用户券。
     *
     * @param req 回退请求
     */
    @PostMapping("/revert-by-order")
    void revertByOrder(
            @RequestBody
            CouponOrderRevertByOrderReq req
    );
}
