package com.cloudmall.order.api.client;

import com.cloudmall.common.entity.Result;
import com.cloudmall.order.api.response.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "order-service", path = "/order")
public interface OrderClient {

    @GetMapping("/{id}")
    Result<OrderResponse> getById(@PathVariable("id") Long id);

    @GetMapping("/list")
    Result<List<OrderResponse>> listByUser(@RequestParam("userId") Long userId);
}
