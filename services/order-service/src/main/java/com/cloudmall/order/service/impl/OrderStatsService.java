package com.cloudmall.order.service.impl;

import java.math.BigDecimal;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.order.api.response.CouponUsageStatsResp;
import com.cloudmall.order.mapper.OrderCouponMapper;
import com.cloudmall.order.service.IOrderStatsService;

/**
 * 订单优惠统计服务实现
 * <p>
 * 基于订单优惠关联快照聚合使用订单数与补贴金额，供 coupon-service 成本统计使用。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class OrderStatsService implements IOrderStatsService {

    private final OrderCouponMapper orderCouponMapper;

    /**
     * 查询指定模板的使用订单数与补贴金额。
     *
     * @param couponId 模板 ID
     * @return 使用统计
     */
    @Override
    public CouponUsageStatsResp usageByCoupon(Long couponId) {
        return toResp(orderCouponMapper.aggregateByCoupon(couponId));
    }

    /**
     * 查询全局使用订单数与补贴金额。
     *
     * @return 使用统计
     */
    @Override
    public CouponUsageStatsResp usageOverview() {
        return toResp(orderCouponMapper.aggregateOverview());
    }

    /**
     * 聚合结果转统计响应。
     *
     * @param row 聚合行
     * @return 统计响应
     */
    private CouponUsageStatsResp toResp(Map<String, Object> row) {
        if (row == null) {
            return CouponUsageStatsResp.builder()
                                       .orderCount(0L)
                                       .subsidyAmount(BigDecimal.ZERO)
                                       .build();
        }
        Object orderCount = row.get("order_count");
        Object subsidy = row.get("subsidy_amount");
        return CouponUsageStatsResp
                .builder()
                .orderCount(orderCount == null ? 0L : ((Number) orderCount).longValue())
                .subsidyAmount(
                        subsidy == null ? BigDecimal.ZERO : new BigDecimal(subsidy.toString())
                )
                .build();
    }
}
