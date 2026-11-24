package com.cloudmall.search.api.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.SpringQueryMap;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.FallbackFailures;
import com.cloudmall.search.api.client.SearchClient;
import com.cloudmall.search.api.model.req.SearchProductReq;
import com.cloudmall.search.api.model.resp.SearchResultResp;

/**
 * 商品搜索客户端兜底工厂
 * <p>
 * 搜索属于辅助查询，但返回空结果会掩盖下游故障，因此这里快速失败。
 *
 * @author : Tomatos
 */
@Slf4j
public class SearchClientFallbackFactory implements FallbackFactory<SearchClient> {

    @Override
    public SearchClient create(Throwable cause) {
        log.warn("商品搜索客户端调用失败，快速失败", cause);
        RuntimeException failure = FallbackFailures.of(cause, BizErrorEnum.SERVICE_UNAVAILABLE);

        return new SearchClient() {
            @Override
            public SearchResultResp search(@SpringQueryMap SearchProductReq req) {
                throw failure;
            }
        };
    }
}
