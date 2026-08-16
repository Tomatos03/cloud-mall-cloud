package com.cloudmall.order.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.cloudmall.order.api.request.OrderEventReq;
import com.cloudmall.order.api.response.CouponUsageStatsResp;

/**
 * 订单内部 Feign 客户端
 * <p>
 * 内部接口统一收敛于 InternalOrderClient（/internal/order），
 * 覆盖订单生命周期事件与订单优惠统计，仅供服务间 Feign 调用。
 *
 * @author : Tomatos
 */
@FeignClient(name = "order-service", contextId = "internalOrderClient", path = "/internal/order")
public interface InternalOrderClient {

    /**
     * 支付成功。
     *
     * @param req 事件请求
     */
    @PostMapping("/paid")
    void paid(@RequestBody OrderEventReq req);

    /**
     * 支付超时关闭。
     *
     * @param req 事件请求
     */
    @PostMapping("/timeout")
    void timeout(@RequestBody OrderEventReq req);

    /**
     * 取消订单（幂等）。
     *
     * @param req 事件请求
     */
    @PostMapping("/cancel")
    void cancel(@RequestBody OrderEventReq req);

    /**
     * 查询单模板订单使用统计。
     *
     * @param couponId 模板 ID
     * @return 使用订单数与补贴金额
     */
    @GetMapping("/coupon-usage")
    CouponUsageStatsResp usageByCoupon(@RequestParam("couponId") Long couponId);

    /**
     * 查询全局订单使用统计。
     *
     * @return 使用订单数与补贴金额
     */
    @GetMapping("/coupon-usage/overview")
    CouponUsageStatsResp usageOverview();
}
