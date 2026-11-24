package com.cloudmall.order.api.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.FallbackFailures;
import com.cloudmall.order.api.client.InternalOrderClient;
import com.cloudmall.order.api.model.req.OrderEventReq;
import com.cloudmall.order.api.model.req.PlainSeckillOrderReq;
import com.cloudmall.order.api.model.resp.CouponUsageStatsResp;

/**
 * 订单内部客户端兜底工厂
 * <p>
 * 订单事件与建单操作必须快速失败，交由调用方 Saga 流程处理。
 *
 * @author : Tomatos
 */
@Slf4j
public class InternalOrderClientFallbackFactory implements FallbackFactory<InternalOrderClient> {

    @Override
    public InternalOrderClient create(Throwable cause) {
        log.warn("订单内部客户端调用失败，快速失败", cause);
        RuntimeException failure = FallbackFailures.of(cause, BizErrorEnum.SERVICE_UNAVAILABLE);

        return new InternalOrderClient() {
            @Override
            public void paid(OrderEventReq req) {
                throw failure;
            }

            @Override
            public void timeout(OrderEventReq req) {
                throw failure;
            }

            @Override
            public void cancel(OrderEventReq req) {
                throw failure;
            }

            @Override
            public String createPlainSeckillOrder(PlainSeckillOrderReq req) {
                throw failure;
            }

            @Override
            public CouponUsageStatsResp usageByCoupon(Long couponId) {
                throw failure;
            }

            @Override
            public CouponUsageStatsResp usageOverview() {
                throw failure;
            }
        };
    }
}
