package com.cloudmall.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.order.api.response.CouponUsageStatsResp;
import com.cloudmall.order.service.IOrderService;

/**
 * 订单优惠统计内部接口
 * <p>
 * 仅供 coupon-service 通过 Feign 调用，不加入网关对外路由。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/order-stats")
public class OrderStatsController {

    private final IOrderService orderStatsService;

    /**
     * 查询指定模板的使用订单数与补贴金额。
     *
     * @param couponId 模板 ID
     * @return 使用统计
     */
    @GetMapping("/coupon-usage")
    public Result<CouponUsageStatsResp> usageByCoupon(
            @RequestParam("couponId")
            Long couponId
    ) {
        return Result.success(orderStatsService.usageByCoupon(couponId));
    }

    /**
     * 查询全局使用订单数与补贴金额。
     *
     * @return 使用统计
     */
    @GetMapping("/coupon-usage/overview")
    public Result<CouponUsageStatsResp> usageOverview() {
        return Result.success(orderStatsService.usageOverview());
    }
}
