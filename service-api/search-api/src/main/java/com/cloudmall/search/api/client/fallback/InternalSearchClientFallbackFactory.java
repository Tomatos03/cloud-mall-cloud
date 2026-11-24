package com.cloudmall.search.api.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.FallbackFailures;
import com.cloudmall.product.api.model.resp.ProductDocResp;
import com.cloudmall.search.api.client.InternalSearchClient;

/**
 * 搜索内部客户端兜底工厂
 * <p>
 * ES 文档写入/删除必须快速失败，避免商品与搜索索引不一致。
 *
 * @author : Tomatos
 */
@Slf4j
public class InternalSearchClientFallbackFactory implements FallbackFactory<InternalSearchClient> {

    @Override
    public InternalSearchClient create(Throwable cause) {
        log.warn("搜索内部客户端调用失败，快速失败", cause);
        RuntimeException failure = FallbackFailures.of(cause, BizErrorEnum.SERVICE_UNAVAILABLE);

        return new InternalSearchClient() {
            @Override
            public void upsert(ProductDocResp doc) {
                throw failure;
            }

            @Override
            public void deleteById(Long id) {
                throw failure;
            }
        };
    }
}
