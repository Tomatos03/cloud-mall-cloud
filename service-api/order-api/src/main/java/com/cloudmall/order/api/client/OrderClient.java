package com.cloudmall.order.api.client;

import com.cloudmall.common.entity.Result;
import com.cloudmall.order.api.response.OrderResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "order-service", path = "/order")
public interface OrderClient {

    @GetMapping("/{id}")
    Result<OrderResp> getById(@PathVariable("id") Long id);

    @GetMapping("/list")
    Result<List<OrderResp>> listByUser(@RequestParam("userId") Long userId);
}
