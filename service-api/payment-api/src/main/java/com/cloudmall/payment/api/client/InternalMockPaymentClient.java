package com.cloudmall.payment.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.payment.api.model.req.MockPaymentCreateReq;
import com.cloudmall.payment.api.model.resp.MockPaymentCreateResp;
import com.cloudmall.payment.api.model.resp.MockPaymentQueryResp;

/**
 * 模拟支付内部 Feign 客户端。
 * <p>
 * 内部接口统一收敛于 InternalMockPaymentClient（/internal/mock-payment），
 * 返回项目统一的 Result 包装，由 cloudmall-feign 自动拆箱；
 * 微信 API v3 风格的 /v3/** 接口仅供模拟第三方协议联调使用。
 *
 * @author : Tomatos
 */
@FeignClient(
        name = "mock-payment-service",
        contextId = "internalMockPaymentClient",
        path = "/internal/mock-payment"
)
public interface InternalMockPaymentClient {

    /**
     * 创建支付单（同一商户订单号幂等）。
     *
     * @param req 下单请求
     * @return 预支付信息
     */
    @PostMapping("/transactions")
    MockPaymentCreateResp create(@RequestBody MockPaymentCreateReq req);

    /**
     * 按商户订单号查询支付单。
     *
     * @param outTradeNo 商户订单号
     * @return 支付单状态
     */
    @GetMapping("/transactions/{outTradeNo}")
    MockPaymentQueryResp query(@PathVariable("outTradeNo") String outTradeNo);

    /**
     * 关闭未支付支付单（幂等）。
     *
     * @param outTradeNo 商户订单号
     */
    @PostMapping("/transactions/{outTradeNo}/close")
    void close(@PathVariable("outTradeNo") String outTradeNo);
}
