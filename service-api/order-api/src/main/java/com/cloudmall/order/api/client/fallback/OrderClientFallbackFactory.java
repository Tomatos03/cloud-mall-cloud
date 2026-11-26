package com.cloudmall.order.api.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.FallbackFailures;
import com.cloudmall.common.model.page.PageResp;
import com.cloudmall.order.api.client.OrderClient;
import com.cloudmall.order.api.model.resp.OrderResp;

/**
 * 订单客户端兜底工厂
 * <p>
 * 订单读取快速失败，避免返回空订单导致后续状态判断错误。
 *
 * @author : Tomatos
 */
@Slf4j
public class OrderClientFallbackFactory implements FallbackFactory<OrderClient> {

    @Override
    public OrderClient create(Throwable cause) {
        log.warn("订单客户端调用失败，快速失败", cause);
        RuntimeException failure = FallbackFailures.of(cause, BizErrorEnum.SERVICE_UNAVAILABLE);

        return new OrderClient() {
            @Override
            public OrderResp getById(Long id) {
                throw failure;
            }

            @Override
            public PageResp<OrderResp> page(Long userId, long current, long size) {
                throw failure;
            }
        };
    }
}
