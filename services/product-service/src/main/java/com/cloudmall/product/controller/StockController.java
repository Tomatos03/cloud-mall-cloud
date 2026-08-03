package com.cloudmall.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.product.api.request.StockConfirmReq;
import com.cloudmall.product.api.request.StockOpReq;
import com.cloudmall.product.service.IStockService;

/**
 * 普通商品库存内部接口
 * <p>
 * 仅供订单服务通过 Feign 调用，不加入网关对外路由。
 * 提供库存预占、支付确认、取消/超时释放和补偿兜底。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/stock")
public class StockController {

    private final IStockService stockService;

    /**
     * 预占库存。
     *
     * @param req 预占请求
     * @return 处理结果
     */
    @PostMapping("/reserve")
    public Result<Void> reserve(
            @RequestBody
            @Valid
            StockOpReq req
    ) {
        stockService.reserve(req);
        return Result.success();
    }

    /**
     * 支付确认（不改库存）。
     *
     * @param req 确认请求
     * @return 处理结果
     */
    @PostMapping("/confirm")
    public Result<Void> confirm(
            @RequestBody
            @Valid
            StockConfirmReq req
    ) {
        stockService.confirm(req);
        return Result.success();
    }

    /**
     * 释放库存。
     *
     * @param req 释放请求
     * @return 处理结果
     */
    @PostMapping("/release")
    public Result<Void> release(
            @RequestBody
            @Valid
            StockOpReq req
    ) {
        stockService.release(req);
        return Result.success();
    }

    /**
     * 补偿释放。
     *
     * @param req 补偿请求
     * @return 处理结果
     */
    @PostMapping("/compensate")
    public Result<Void> compensate(
            @RequestBody
            @Valid
            StockOpReq req
    ) {
        stockService.compensate(req);
        return Result.success();
    }
}
