package com.cloudmall.coupon.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.coupon.api.request.CouponOrderCommitReq;
import com.cloudmall.coupon.api.request.CouponOrderLockReq;
import com.cloudmall.coupon.api.request.CouponOrderQuoteReq;
import com.cloudmall.coupon.api.request.CouponOrderReleaseByOrderReq;
import com.cloudmall.coupon.api.request.CouponOrderReleaseReq;
import com.cloudmall.coupon.api.request.CouponOrderRevertReq;
import com.cloudmall.coupon.api.response.CouponOrderCalcResp;

/**
 * 优惠券订单协同 Feign 客户端
 * <p>
 * 内部接口，仅供订单服务调用；不加入网关对外路由。
 * 用户身份显式传入（异步场景无用户上下文），不信任客户端金额。
 *
 * @author : Tomatos
 */
@FeignClient(
        name = "coupon-service",
        contextId = "couponOrderClient",
        path = "/internal/coupon-orders"
)
public interface CouponOrderClient {

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
     * @return 处理结果
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
     * @return 处理结果
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
     * @return 处理结果
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
     * @return 处理结果
     */
    @PostMapping("/release-by-order")
    void releaseByOrder(
            @RequestBody
            CouponOrderReleaseByOrderReq req
    );
}
