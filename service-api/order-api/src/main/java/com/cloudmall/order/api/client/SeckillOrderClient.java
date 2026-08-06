package com.cloudmall.order.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.order.api.request.SeckillOrderCreateReq;
import com.cloudmall.order.api.response.OrderResp;

/**
 * 秒杀订单建单 Feign 客户端
 * <p>
 * 供 consumer-service 消费秒杀建单事件后调用，按订单号幂等。
 *
 * @author : Tomatos
 */
@FeignClient(
        name = "order-service",
        contextId = "seckillOrderCreateClient",
        path = "/internal/seckill-orders"
)
public interface SeckillOrderClient {

    /**
     * 创建秒杀订单（幂等，重复投递返回已有订单）。
     *
     * @param req 建单请求
     * @return 订单响应
     */
    @PostMapping
    OrderResp createSeckillOrder(
            @RequestBody
            SeckillOrderCreateReq req
    );
}
