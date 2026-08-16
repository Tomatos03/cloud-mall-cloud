package com.cloudmall.product.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.product.api.request.StockConfirmReq;
import com.cloudmall.product.api.request.StockOpReq;
import com.cloudmall.product.api.request.StockReleaseByOrderReq;

/**
 * 商品内部 Feign 客户端
 * <p>
 * 内部接口统一收敛于 InternalProductClient（/internal/product），
 * 提供库存预占、支付确认、取消/超时释放和补偿兜底，仅供服务间 Feign 调用。
 *
 * @author : Tomatos
 */
@FeignClient(name = "product-service", contextId = "internalProductClient", path = "/internal/product")
public interface InternalProductClient {

    /**
     * 预占库存。
     *
     * @param req 预占请求
     */
    @PostMapping("/reserve")
    void reserve(@RequestBody StockOpReq req);

    /**
     * 支付确认（不改库存）。
     *
     * @param req 确认请求
     */
    @PostMapping("/confirm")
    void confirm(@RequestBody StockConfirmReq req);

    /**
     * 释放库存。
     *
     * @param req 释放请求
     */
    @PostMapping("/release")
    void release(@RequestBody StockOpReq req);

    /**
     * 补偿释放。
     *
     * @param req 补偿请求
     */
    @PostMapping("/compensate")
    void compensate(@RequestBody StockOpReq req);

    /**
     * 按订单释放全部预占库存。
     *
     * @param req 释放请求
     */
    @PostMapping("/release-by-order")
    void releaseByOrder(@RequestBody StockReleaseByOrderReq req);
}
