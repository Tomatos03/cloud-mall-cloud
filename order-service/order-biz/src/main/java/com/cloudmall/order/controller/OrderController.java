package com.cloudmall.order.controller;

import com.cloudmall.common.entity.Result;
import com.cloudmall.order.api.request.CreateReq;
import com.cloudmall.order.api.response.OrderResp;
import com.cloudmall.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    @PostMapping
    public Result<OrderResp> create(@RequestBody CreateReq request) {
        return Result.success(orderService.createOrder(request));
    }

    @GetMapping("/{id}")
    public Result<OrderResp> getById(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @GetMapping("/list")
    public Result<List<OrderResp>> listByUser(@RequestParam Long userId,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(orderService.listByUser(userId, page, size));
    }
}
