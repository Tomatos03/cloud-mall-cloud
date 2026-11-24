package com.cloudmall.product.api.client.fallback;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.FallbackFailures;
import com.cloudmall.product.api.client.SkuClient;
import com.cloudmall.product.api.model.req.StockReq;
import com.cloudmall.product.api.model.resp.SkuResp;

/**
 * SKU 客户端兜底工厂
 * <p>
 * SKU 库存扣减/回滚与价格查询均快速失败，禁止返回空列表或 false。
 *
 * @author : Tomatos
 */
@Slf4j
public class SkuClientFallbackFactory implements FallbackFactory<SkuClient> {

    @Override
    public SkuClient create(Throwable cause) {
        log.warn("SKU 客户端调用失败，快速失败", cause);
        RuntimeException failure = FallbackFailures.of(cause, BizErrorEnum.SERVICE_UNAVAILABLE);

        return new SkuClient() {
            @Override
            public Boolean deductStock(Long skuId, StockReq req) {
                throw failure;
            }

            @Override
            public Boolean releaseStock(Long skuId, StockReq req) {
                throw failure;
            }

            @Override
            public List<SkuResp> listSkusByIds(List<Long> skuIds) {
                throw failure;
            }

            @Override
            public SkuResp getById(Long skuId) {
                throw failure;
            }
        };
    }
}
