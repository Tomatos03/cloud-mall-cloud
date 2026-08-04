package com.cloudmall.order.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cloudmall.order.api.response.CouponUsageStatsResp;

/**
 * 订单优惠统计 Feign 客户端
 * <p>
 * 内部接口，仅供 coupon-service 统计成本维度调用。
 *
 * @author : Tomatos
 */
@FeignClient(name = "order-service", contextId = "orderStatsClient", path = "/internal/order-stats")
public interface OrderStatsClient {

    /**
     * 查询单模板订单使用统计。
     *
     * @param couponId 模板 ID
     * @return 使用订单数与补贴金额
     */
    @GetMapping("/coupon-usage")
    CouponUsageStatsResp usageByCoupon(
            @RequestParam("couponId")
            Long couponId
    );

    /**
     * 查询全局订单使用统计。
     *
     * @return 使用订单数与补贴金额
     */
    @GetMapping("/coupon-usage/overview")
    CouponUsageStatsResp usageOverview();
}
