package com.cloudmall.order.api.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.cloudmall.common.model.Result;
import com.cloudmall.order.api.response.OrderResp;

@FeignClient(name = "order-service", path = "/order")
public interface OrderClient {

    @GetMapping("/{id}")
    Result<OrderResp> getById(@PathVariable("id") Long id);

    @GetMapping
    Result<Page<OrderResp>> page(@RequestParam("userId") Long userId,
                                 @RequestParam(value = "current", defaultValue = "1") long current,
                                 @RequestParam(value = "size", defaultValue = "20") long size);
}