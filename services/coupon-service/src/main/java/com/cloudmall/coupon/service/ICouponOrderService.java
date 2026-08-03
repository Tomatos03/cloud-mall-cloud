package com.cloudmall.coupon.service;

import com.cloudmall.coupon.api.request.CouponOrderCommitReq;
import com.cloudmall.coupon.api.request.CouponOrderLockReq;
import com.cloudmall.coupon.api.request.CouponOrderQuoteReq;
import com.cloudmall.coupon.api.request.CouponOrderReleaseByOrderReq;
import com.cloudmall.coupon.api.request.CouponOrderReleaseReq;
import com.cloudmall.coupon.api.request.CouponOrderRevertReq;
import com.cloudmall.coupon.api.response.CouponOrderCalcResp;

/**
 * 订单协同内部服务
 * <p>
 * 供 order-service 通过 Feign 调用，不对外路由：
 * quote 无副作用试算；lock 重新计算并锁定用户券；
 * commit/release/revert 由支付、超时、取消、全额退款事件驱动。
 * 幂等键：lock = 订单号，commit/release = 订单号，revert = 订单号 + 退款单号。
 *
 * @author : Tomatos
 */
public interface ICouponOrderService {

    /**
     * 按订单释放全部已锁定用户券（取消/超时/补偿场景，幂等）。
     *
     * @param req 释放请求
     */
    void releaseByOrder(CouponOrderReleaseByOrderReq req);

    /**
     * 无副作用试算：按用户选择计算优惠快照，并返回候选券与推荐组合。
     *
     * @param req 试算请求
     * @return 优惠快照
     */
    CouponOrderCalcResp quote(CouponOrderQuoteReq req);

    /**
     * 锁定：重新计算并锁定用户选择的用户券（订单号幂等）。
     *
     * @param req 锁定请求
     * @return 优惠快照
     */
    CouponOrderCalcResp lock(CouponOrderLockReq req);

    /**
     * 支付成功核销：LOCKED → USED（订单号幂等）。
     *
     * @param req 核销请求
     */
    void commit(CouponOrderCommitReq req);

    /**
     * 取消/超时释放：LOCKED → AVAILABLE/EXPIRED（订单号幂等）。
     *
     * @param req 释放请求
     */
    void release(CouponOrderReleaseReq req);

    /**
     * 全额退款回退：USED → AVAILABLE/EXPIRED（订单号 + 退款单号幂等）。
     *
     * @param req 回退请求
     */
    void revert(CouponOrderRevertReq req);
}
