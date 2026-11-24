package com.cloudmall.coupon.api.client.fallback;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.FallbackFailures;
import com.cloudmall.coupon.api.client.CouponClient;
import com.cloudmall.coupon.api.model.resp.CouponResp;

/**
 * 优惠券用户端客户端兜底工厂
 * <p>
 * 核心调用快速失败，不返回空列表，避免上层误判为“无可用优惠券”。
 *
 * @author : Tomatos
 */
@Slf4j
public class CouponClientFallbackFactory implements FallbackFactory<CouponClient> {

    @Override
    public CouponClient create(Throwable cause) {
        log.warn("优惠券客户端调用失败，快速失败", cause);
        RuntimeException failure = FallbackFailures.of(cause, BizErrorEnum.SERVICE_UNAVAILABLE);

        return new CouponClient() {
            @Override
            public List<CouponResp> listPublic() {
                throw failure;
            }
        };
    }
}
