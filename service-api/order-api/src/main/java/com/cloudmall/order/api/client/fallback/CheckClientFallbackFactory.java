package com.cloudmall.order.api.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.FallbackFailures;
import com.cloudmall.order.api.client.CheckClient;
import com.cloudmall.order.api.enums.CheckoutTypeEnum;
import com.cloudmall.order.api.model.req.CheckoutReq;

/**
 * 结算客户端兜底工厂
 * <p>
 * 建单失败快速失败，避免上层误判为建单成功。
 *
 * @author : Tomatos
 */
@Slf4j
public class CheckClientFallbackFactory implements FallbackFactory<CheckClient> {

    @Override
    public CheckClient create(Throwable cause) {
        log.warn("结算客户端调用失败，快速失败", cause);
        RuntimeException failure = FallbackFailures.of(cause, BizErrorEnum.SERVICE_UNAVAILABLE);

        return new CheckClient() {
            @Override
            public String checkout(CheckoutTypeEnum type, CheckoutReq req) {
                throw failure;
            }
        };
    }
}
