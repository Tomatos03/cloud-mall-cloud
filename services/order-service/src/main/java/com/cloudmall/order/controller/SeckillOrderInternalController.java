package com.cloudmall.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.order.api.request.SeckillOrderCreateReq;
import com.cloudmall.order.api.response.OrderResp;
import com.cloudmall.order.service.ISeckillOrderCreateService;

/**
 * 秒杀订单内部控制器
 * <p>
 * 仅供 consumer-service 消费秒杀建单事件后调用，不经网关对外暴露。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/seckill-orders")
public class SeckillOrderInternalController {

    private final ISeckillOrderCreateService seckillOrderCreateService;

    /**
     * 创建秒杀订单（幂等）。
     *
     * @param req 建单请求
     * @return 订单响应
     */
    @PostMapping
    public Result<OrderResp> createSeckillOrder(
            @Valid
            @RequestBody
            SeckillOrderCreateReq req
    ) {
        return Result.success(seckillOrderCreateService.createSeckillOrder(req));
    }
}
