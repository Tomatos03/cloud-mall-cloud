package com.cloudmall.order.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.order.api.request.OrderEventReq;

/**
 * 订单生命周期事件 Feign 客户端
 * <p>
 * 内部接口，供秒杀等上游服务触发订单支付/超时/取消/退款事件，均为幂等调用。
 *
 * @author : Tomatos
 */
@FeignClient(name = "order-service", contextId = "orderEventClient", path = "/internal/order-events")
public interface OrderEventClient {

    /**
     * 支付成功。
     *
     * @param req 事件请求
     */
    @PostMapping("/paid")
    void paid(
            @RequestBody
            OrderEventReq req
    );

    /**
     * 支付超时关闭。
     *
     * @param req 事件请求
     */
    @PostMapping("/timeout")
    void timeout(
            @RequestBody
            OrderEventReq req
    );

    /**
     * 取消订单（幂等）。
     *
     * @param req 事件请求
     */
    @PostMapping("/cancel")
    void cancel(
            @RequestBody
            OrderEventReq req
    );
}
