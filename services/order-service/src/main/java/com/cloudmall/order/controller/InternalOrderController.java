package com.cloudmall.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.order.api.request.OrderEventReq;
import com.cloudmall.order.api.response.CouponUsageStatsResp;
import com.cloudmall.order.service.IOrderEventService;
import com.cloudmall.order.service.IOrderService;

/**
 * 订单内部接口
 * <p>
 * 内部服务端点统一使用 /internal/order，仅供服务间 Feign 调用，不加入网关对外路由。
 * 支付成功/超时/取消/全额退款的状态变更通过 RocketMQ 事务消息发送，
 * 本地事务由 OrderTxMessageListener 执行，coupon-service 消费后核销/释放/回退用户券。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/order")
public class InternalOrderController {

    private final IOrderEventService orderEventService;
    private final IOrderService orderStatsService;

    /**
     * 支付成功。
     *
     * @param req 事件请求
     * @return 处理结果
     */
    @PostMapping("/paid")
    public Result<Void> paid(@RequestBody @Valid OrderEventReq req) {
        orderEventService.handlePaid(req.getOrderNo(), req.getPayAmount());
        return Result.success();
    }

    /**
     * 支付超时。
     *
     * @param req 事件请求
     * @return 处理结果
     */
    @PostMapping("/timeout")
    public Result<Void> timeout(@RequestBody @Valid OrderEventReq req) {
        orderEventService.handleTimeout(req.getOrderNo());
        return Result.success();
    }

    /**
     * 订单取消。
     *
     * @param req 事件请求
     * @return 处理结果
     */
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestBody @Valid OrderEventReq req) {
        orderEventService.handleCancel(req.getOrderNo());
        return Result.success();
    }

    /**
     * 全额退款。
     *
     * @param req 事件请求
     * @return 处理结果
     */
    @PostMapping("/refund")
    public Result<Void> refund(@RequestBody @Valid OrderEventReq req) {
        orderEventService.handleFullyRefunded(req.getOrderNo(), req.getRefundId(), req.getRefundAmount());
        return Result.success();
    }

    /**
     * 查询指定模板的使用订单数与补贴金额。
     *
     * @param couponId 模板 ID
     * @return 使用统计
     */
    @GetMapping("/coupon-usage")
    public Result<CouponUsageStatsResp> usageByCoupon(@RequestParam("couponId") Long couponId) {
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
