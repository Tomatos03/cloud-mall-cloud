package com.cloudmall.search.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

import com.cloudmall.search.api.model.req.SearchProductReq;
import com.cloudmall.search.api.model.resp.SearchResultResp;

/**
 * 商品搜索 Feign 客户端，供 AI 客服按关键词查询商品。
 */
@FeignClient(name = "search-service", contextId = "searchClient", path = "/search")
public interface SearchClient {

    /**
     * 搜索商品。
     *
     * @param req 搜索请求
     * @return 搜索结果
     */
    @GetMapping("/products")
    SearchResultResp search(@SpringQueryMap SearchProductReq req);
}
