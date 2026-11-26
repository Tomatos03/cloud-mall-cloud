package com.cloudmall.cart.api.client.fallback;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.cart.api.client.InternalCartClient;
import com.cloudmall.cart.api.model.resp.CartSelectedItemResp;
import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.FallbackFailures;

/**
 * 购物车内部客户端兜底工厂
 * <p>
 * 下单读购物车与清车都快速失败，避免用空数据继续下单或误判清车成功。
 *
 * @author : Tomatos
 */
@Slf4j
public class InternalCartClientFallbackFactory implements FallbackFactory<InternalCartClient> {

    @Override
    public InternalCartClient create(Throwable cause) {
        log.warn("购物车客户端调用失败，快速失败", cause);
        RuntimeException failure = FallbackFailures.of(cause, BizErrorEnum.SERVICE_UNAVAILABLE);

        return new InternalCartClient() {
            @Override
            public List<CartSelectedItemResp> listSelectedItems(Long userId) {
                throw failure;
            }

            @Override
            public void removeItems(Long userId, List<Long> skuIds) {
                throw failure;
            }
        };
    }
}
