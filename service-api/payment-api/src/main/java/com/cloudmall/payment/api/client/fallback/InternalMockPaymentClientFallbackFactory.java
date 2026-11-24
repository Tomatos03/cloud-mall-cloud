package com.cloudmall.payment.api.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.FallbackFailures;
import com.cloudmall.payment.api.client.InternalMockPaymentClient;
import com.cloudmall.payment.api.model.req.MockPaymentCreateReq;
import com.cloudmall.payment.api.model.resp.MockPaymentCreateResp;
import com.cloudmall.payment.api.model.resp.MockPaymentQueryResp;

/**
 * 模拟支付内部客户端兜底工厂
 * <p>
 * 支付单创建、查询与关单均快速失败，禁止返回默认支付状态。
 *
 * @author : Tomatos
 */
@Slf4j
public class InternalMockPaymentClientFallbackFactory implements FallbackFactory<InternalMockPaymentClient> {

    @Override
    public InternalMockPaymentClient create(Throwable cause) {
        log.warn("模拟支付内部客户端调用失败，快速失败", cause);
        RuntimeException failure = FallbackFailures.of(cause, BizErrorEnum.SERVICE_UNAVAILABLE);

        return new InternalMockPaymentClient() {
            @Override
            public MockPaymentCreateResp create(MockPaymentCreateReq req) {
                throw failure;
            }

            @Override
            public MockPaymentQueryResp query(String outTradeNo) {
                throw failure;
            }

            @Override
            public void close(String outTradeNo) {
                throw failure;
            }
        };
    }
}
