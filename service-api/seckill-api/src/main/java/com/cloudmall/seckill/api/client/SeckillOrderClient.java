package com.cloudmall.seckill.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.seckill.api.request.SeckillConfirmPaidReq;
import com.cloudmall.seckill.api.response.SeckillOrderResp;

/**
 * 秒杀单内部 Feign 客户端
 * <p>
 * 供 consumer-service 在订单支付成功后回调秒杀侧确认，以及对账查询。
 *
 * @author : Tomatos
 */
@FeignClient(name = "seckill-service", contextId = "seckillOrderClient", path = "/internal/seckill")
public interface SeckillOrderClient {

    /**
     * 支付成功确认：秒杀单置 PAID 并落库扣减秒杀池（幂等）。
     *
     * @param req 确认请求
     */
    @PostMapping("/confirm-paid")
    void confirmPaid(
            @RequestBody
            SeckillConfirmPaidReq req
    );

    /**
     * 按真实订单号查询秒杀单。
     *
     * @param orderNo 真实订单号
     * @return 秒杀单响应
     */
    @GetMapping("/orders/by-order-no/{orderNo}")
    SeckillOrderResp getByOrderNo(
            @PathVariable("orderNo")
            String orderNo
    );
}
