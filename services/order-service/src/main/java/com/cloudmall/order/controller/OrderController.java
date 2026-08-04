package com.cloudmall.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.coupon.api.response.CouponOrderCalcResp;
import com.cloudmall.order.api.request.CreateOrderReq;
import com.cloudmall.order.api.request.QuoteOrderReq;
import com.cloudmall.order.api.response.OrderResp;
import com.cloudmall.order.api.response.OrderSubResp;
import com.cloudmall.order.model.req.OrderPageReq;
import com.cloudmall.order.model.req.OrderSubPageReq;
import com.cloudmall.order.service.IOrderService;

/**
 * 订单接口
 * <p>
 * 用户下单与查询自己的父订单；商家查询自己店铺子订单；运营查询全量。
 * userId 一律从用户上下文获取，不信任请求参数。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final IOrderService orderService;

    /**
     * 创建订单（跨店结算 Saga）。
     *
     * @param req 下单请求
     * @return 父订单详情
     */
    @PostMapping
    public Result<OrderResp> create(
            @RequestBody
            @Valid
            CreateOrderReq req
    ) {
        return Result.success(orderService.create(req));
    }

    /**
     * 下单前价格预览（无副作用）。
     *
     * @param req 预览请求（只需商品 SKU 与数量）
     * @return 三级价格明细 + 优惠券使用明细 + 推荐组合
     */
    @PostMapping("/quote")
    public Result<CouponOrderCalcResp> quote(
            @RequestBody
            @Valid
            QuoteOrderReq req
    ) {
        return Result.success(orderService.quote(req));
    }

    /**
     * 获取父订单详情（仅当前用户本人可查）。
     *
     * @param id 父订单 ID
     * @return 父订单详情
     */
    @GetMapping("/{id}")
    public Result<OrderResp> getById(
            @PathVariable
            Long id
    ) {
        return Result.success(orderService.getById(id));
    }

    /**
     * 当前用户分页查询自己的父订单。
     *
     * @param req 分页请求
     * @return 父订单分页
     */
    @GetMapping
    public Result<IPage<OrderResp>> page(OrderPageReq req) {
        return Result.success(orderService.pageUser(req));
    }

    /**
     * 当前商家分页查询自己店铺的子订单。
     *
     * @param req 分页请求（shopId 由服务层注入）
     * @return 子订单分页
     */
    @GetMapping("/merchant")
    public Result<IPage<OrderSubResp>> merchantPage(OrderSubPageReq req) {
        return Result.success(orderService.pageMerchant(req));
    }

    /**
     * 运营分页查询全量父订单。
     *
     * @param req 分页请求（可含 userId 过滤）
     * @return 父订单分页
     */
    @GetMapping("/operator")
    public Result<IPage<OrderResp>> operatorPage(OrderPageReq req) {
        return Result.success(orderService.pageOperator(req));
    }
}
