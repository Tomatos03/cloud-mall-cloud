package com.cloudmall.seckill.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.seckill.api.request.SeckillConfirmPaidReq;
import com.cloudmall.seckill.api.response.SeckillOrderResp;

/**
 * 秒杀内部 Feign 客户端
 * <p>
 * 内部接口统一收敛于 InternalSeckillClient（/internal/seckill），
 * 覆盖支付成功确认、对账查询与活动商品审核校验，仅供服务间 Feign 调用。
 *
 * @author : Tomatos
 */
@FeignClient(name = "seckill-service", contextId = "internalSeckillClient", path = "/internal/seckill")
public interface InternalSeckillClient {

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
    @GetMapping("/orders/{orderNo}")
    SeckillOrderResp getByOrderNo(
            @PathVariable("orderNo")
            String orderNo
    );

    /**
     * 校验活动下的秒杀商品是否已审核通过。
     *
     * @param activityId 活动 ID
     * @param productId 秒杀商品 ID
     * @return 已审核通过返回 true
     */
    @GetMapping("/activities/{activityId}/products/{productId}/verification")
    Boolean verifyProduct(
            @PathVariable("activityId")
            Long activityId,
            @PathVariable("productId")
            Long productId
    );
}
