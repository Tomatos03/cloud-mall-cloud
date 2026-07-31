package com.cloudmall.coupon.service;

import java.util.List;

import com.cloudmall.coupon.api.request.ClaimReq;
import com.cloudmall.coupon.api.response.CouponResp;

public interface ICouponService {

    /**
     * 查询数据列表。
     *
     * @return 数据列表
     */
    List<CouponResp> listAvailable();

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     */
    CouponResp getById(Long id);

    /**
     * 领取优惠券。
     *
     * @param request 请求参数
     * @return 是否满足条件
     */
    Boolean claim(ClaimReq request);

    /**
     * 校验相关数据。
     *
     * @param couponId 优惠券标识
     * @param userId 用户标识
     * @return 处理结果
     */
    CouponResp verifyCoupon(Long couponId, Long userId);

    /**
     * 标记优惠券为已使用。
     *
     * @param id 对象标识
     */
    void markUsed(Long id);
}