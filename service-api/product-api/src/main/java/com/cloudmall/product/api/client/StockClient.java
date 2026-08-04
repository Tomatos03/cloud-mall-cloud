package com.cloudmall.product.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.product.api.request.StockConfirmReq;
import com.cloudmall.product.api.request.StockOpReq;
import com.cloudmall.product.api.request.StockReleaseByOrderReq;

/**
 * 普通商品库存 Feign 客户端
 * <p>
 * 内部接口，仅供订单服务调用；同一订单号同一 SKU 同一操作类型幂等。
 *
 * @author : Tomatos
 */
@FeignClient(name = "product-service", contextId = "stockClient", path = "/internal/stock")
public interface StockClient {

    /**
     * 预占库存。
     *
     * @param req 预占请求
     * @return 处理结果
     */
    @PostMapping("/reserve")
    void reserve(
            @RequestBody
            StockOpReq req
    );

    /**
     * 支付确认（不改库存）。
     *
     * @param req 确认请求
     * @return 处理结果
     */
    @PostMapping("/confirm")
    void confirm(
            @RequestBody
            StockConfirmReq req
    );

    /**
     * 释放库存。
     *
     * @param req 释放请求
     * @return 处理结果
     */
    @PostMapping("/release")
    void release(
            @RequestBody
            StockOpReq req
    );

    /**
     * 补偿释放。
     *
     * @param req 补偿请求
     * @return 处理结果
     */
    @PostMapping("/compensate")
    void compensate(
            @RequestBody
            StockOpReq req
    );

    /**
     * 按订单释放全部预占库存。
     *
     * @param req 释放请求
     * @return 处理结果
     */
    @PostMapping("/release-by-order")
    void releaseByOrder(
            @RequestBody
            StockReleaseByOrderReq req
    );
}
