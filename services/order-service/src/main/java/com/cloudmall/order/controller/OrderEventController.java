package com.cloudmall.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.order.api.request.OrderEventReq;
import com.cloudmall.order.service.IOrderEventService;

/**
 * 订单事件内部接口
 * <p>
 * 支付成功/超时/取消/全额退款的模拟入口，状态变更与 Outbox 同事务提交，
 * 事件由发布任务异步发送，coupon-service 消费后核销/释放/回退用户券。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/order-events")
public class OrderEventController {

    private final IOrderEventService orderEventService;

    /**
     * 支付成功。
     *
     * @param req 事件请求
     * @return 处理结果
     */
    @PostMapping("/paid")
    public Result<Void> paid(
            @RequestBody
            @Valid
            OrderEventReq req
    ) {
        orderEventService.handlePaid(req.getOrderNo());
        return Result.success();
    }

    /**
     * 支付超时。
     *
     * @param req 事件请求
     * @return 处理结果
     */
    @PostMapping("/timeout")
    public Result<Void> timeout(
            @RequestBody
            @Valid
            OrderEventReq req
    ) {
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
    public Result<Void> cancel(
            @RequestBody
            @Valid
            OrderEventReq req
    ) {
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
    public Result<Void> refund(
            @RequestBody
            @Valid
            OrderEventReq req
    ) {
        orderEventService.handleFullyRefunded(req.getOrderNo(), req.getRefundId());
        return Result.success();
    }
}
