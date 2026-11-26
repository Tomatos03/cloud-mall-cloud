package com.cloudmall.order.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.order.api.client.fallback.CheckClientFallbackFactory;
import com.cloudmall.order.api.enums.CheckoutTypeEnum;
import com.cloudmall.order.api.model.req.CheckoutReq;

/**
 * 结算客户端
 * <p>
 * 供内部服务调用统一建单入口；订单查询仍走 {@link OrderClient}。
 *
 * @author : Tomatos
 */
@FeignClient(
        name = "order-service",
        contextId = "checkClient",
        path = "/checkouts",
        fallbackFactory = CheckClientFallbackFactory.class
)
public interface CheckClient {

    /**
     * 创建订单（按结算类型分发购物车结算/正常结算/秒杀结算）。
     *
     * @param type 结算类型
     * @param req 建单请求
     * @return 订单编号
     */
    @PostMapping("/{type}")
    String checkout(@PathVariable("type") CheckoutTypeEnum type, @RequestBody CheckoutReq req);
}
