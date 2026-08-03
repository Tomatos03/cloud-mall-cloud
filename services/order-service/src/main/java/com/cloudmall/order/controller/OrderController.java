package com.cloudmall.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.order.api.request.CreateOrderReq;
import com.cloudmall.order.api.response.OrderResp;
import com.cloudmall.order.api.response.OrderSubResp;
import com.cloudmall.order.model.req.OrderPageReq;
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
        return Result.success(orderService.createOrder(req));
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
     * @param userId 旧客户端兼容参数，忽略（以用户上下文为准）
     * @param current 当前页码
     * @param size 页大小
     * @return 父订单分页
     */
    @GetMapping
    public Result<IPage<OrderResp>> page(
            @RequestParam(required = false)
            Long userId,
            @RequestParam(value = "current", defaultValue = "1")
            long current,
            @RequestParam(value = "size", defaultValue = "20")
            long size
    ) {
        return Result.success(orderService.pageUser(new OrderPageReq(current, size)));
    }

    /**
     * 当前商家分页查询自己店铺的子订单。
     *
     * @param current 当前页码
     * @param size 页大小
     * @return 子订单分页
     */
    @GetMapping("/merchant")
    public Result<IPage<OrderSubResp>> merchantPage(
            @RequestParam(value = "current", defaultValue = "1")
            long current,
            @RequestParam(value = "size", defaultValue = "20")
            long size
    ) {
        return Result.success(orderService.pageMerchant(current, size));
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
