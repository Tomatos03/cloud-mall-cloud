package com.cloudmall.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.page.PageResp;
import com.cloudmall.common.model.result.Result;
import com.cloudmall.order.api.model.req.OrderPageReq;
import com.cloudmall.order.api.model.req.OrderSubPageReq;
import com.cloudmall.order.api.model.resp.OrderResp;
import com.cloudmall.order.api.model.resp.OrderSubResp;
import com.cloudmall.order.service.IOrderService;

/**
 * 订单接口
 * <p>
 * 用户查询自己的父订单；商家查询自己店铺子订单；运营查询全量。
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
     * 获取父订单详情（仅当前用户本人可查）。
     *
     * @param id 父订单 ID
     * @return 父订单详情
     */
    @GetMapping("/{id}")
    public Result<OrderResp> getById(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    /**
     * 当前用户分页查询自己的父订单（userId 从用户上下文注入）。
     *
     * @param req 分页请求
     * @return 父订单分页
     */
    @GetMapping
    public Result<PageResp<OrderResp>> page(OrderPageReq req) {
        Long userId = UserContextHolder.getUserId();
        req.setUserId(userId);
        return Result.success(orderService.page(req));
    }

    /**
     * 当前商家分页查询自己店铺的子订单。
     *
     * @param req 分页请求（shopId 由服务层注入）
     * @return 子订单分页
     */
    @GetMapping("/merchant")
    public Result<PageResp<OrderSubResp>> merchantPage(OrderSubPageReq req) {
        return Result.success(orderService.pageMerchant(req));
    }

    /**
     * 运营分页查询全量父订单（条件由调用方组装，可含 userId 过滤）。
     *
     * @param req 分页请求（可含 userId 过滤）
     * @return 父订单分页
     */
    @GetMapping("/operator")
    public Result<PageResp<OrderResp>> operatorPage(OrderPageReq req) {
        return Result.success(orderService.page(req));
    }
}
