package com.cloudmall.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.Result;
import com.cloudmall.order.service.UserOrderActionService;

/**
 * 用户侧订单操作接口。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class UserOrderActionController {

    private final UserOrderActionService userOrderActionService;

    /**
     * 确认收货。
     *
     * @param orderNo 子订单号
     * @return 处理结果
     */
    @PostMapping("/{orderNo}/receive")
    public Result<Void> receive(@PathVariable String orderNo) {
        userOrderActionService.receive(UserContextHolder.getUserId(), orderNo);
        return Result.success();
    }

    /**
     * 取消未支付订单。
     *
     * @param orderNo 父订单号
     * @return 处理结果
     */
    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo) {
        userOrderActionService.cancel(UserContextHolder.getUserId(), orderNo);
        return Result.success();
    }
}
