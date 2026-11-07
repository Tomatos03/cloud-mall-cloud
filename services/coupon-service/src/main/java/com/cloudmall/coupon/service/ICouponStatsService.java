package com.cloudmall.coupon.service;

import com.cloudmall.coupon.api.model.resp.CouponStatsResp;

/**
 * 优惠券统计服务
 * <p>
 * 模板维度统计基于用户券流水，成本维度统计基于订单优惠快照（Feign）。
 *
 * @author : Tomatos
 */
public interface ICouponStatsService {

    /**
     * 统计指定模板的发行、核销与补贴。
     *
     * @param couponId 模板 ID
     * @return 模板统计
     */
    CouponStatsResp statsByCoupon(Long couponId);

    /**
     * 统计全平台发行、核销与补贴。
     *
     * @return 全局概览
     */
    CouponStatsResp overview();
}
