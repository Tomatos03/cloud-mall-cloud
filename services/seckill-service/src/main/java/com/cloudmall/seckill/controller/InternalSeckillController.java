package com.cloudmall.seckill.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.seckill.api.request.SeckillConfirmPaidReq;
import com.cloudmall.seckill.api.response.SeckillOrderResp;
import com.cloudmall.seckill.convert.SeckillConverter;
import com.cloudmall.seckill.service.ISeckillOrderService;
import com.cloudmall.seckill.service.ISeckillProductService;

/**
 * 秒杀内部接口控制器
 * <p>
 * 仅供服务间 Feign 调用。挂在 /internal 前缀下而非 /seckill/internal：
 * 网关路由 Path=/seckill/** 会把后者暴露到公网，而 RBAC 对未配规则的路径默认放行，
 * 等于任何人都能调支付确认。与 order/coupon 的 /internal/** 约定保持一致。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/seckill")
public class InternalSeckillController {

    private final ISeckillOrderService orderService;
    private final ISeckillProductService productService;
    private final SeckillConverter seckillConverter;

    /**
     * 支付成功确认（consumer 消费 OrderPaid 事件后调用）。
     *
     * @param req 确认请求
     * @return 处理结果
     */
    @PostMapping("/confirm-paid")
    public Result<Void> confirmPaid(@RequestBody SeckillConfirmPaidReq req) {
        orderService.confirmPaid(req.getOrderNo());
        return Result.success(null);
    }

    /**
     * 按真实订单号查询秒杀单。
     *
     * @param orderNo 真实订单号
     * @return 秒杀单响应
     */
    @GetMapping("/orders/{orderNo}")
    public Result<SeckillOrderResp> getByOrderNo(@PathVariable String orderNo) {
        return Result.success(seckillConverter.toSeckillOrderResp(orderService.getByOrderNo(orderNo)));
    }

    /**
     * 校验活动下的秒杀商品是否已审核通过。
     *
     * @param activityId 活动 ID
     * @param productId 秒杀商品 ID
     * @return 已审核通过返回 true
     */
    @GetMapping("/activities/{activityId}/products/{productId}/verification")
    public Result<Boolean> verifyProduct(@PathVariable Long activityId, @PathVariable Long productId) {
        return Result.success(productService.isApproved(activityId, productId));
    }
}
