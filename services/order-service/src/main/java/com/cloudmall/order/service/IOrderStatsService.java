package com.cloudmall.order.service;

import com.cloudmall.order.api.response.CouponUsageStatsResp;

/**
 * 订单优惠统计服务
 * <p>
 * 基于订单优惠关联快照聚合使用订单数与补贴金额。
 *
 * @author : Tomatos
 */
public interface IOrderStatsService {

    /**
     * 查询指定模板的使用订单数与补贴金额。
     *
     * @param couponId 模板 ID
     * @return 使用统计
     */
    CouponUsageStatsResp usageByCoupon(Long couponId);

    /**
     * 查询全局使用订单数与补贴金额。
     *
     * @return 使用统计
     */
    CouponUsageStatsResp usageOverview();
}
