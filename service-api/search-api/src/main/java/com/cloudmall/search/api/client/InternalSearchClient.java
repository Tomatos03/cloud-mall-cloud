package com.cloudmall.search.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.product.api.model.resp.ProductDocResp;

/**
 * 搜索内部 Feign 客户端
 * <p>
 * 内部接口统一收敛于 InternalSearchClient（/internal/search），
 * 承接 ES 商品索引的写入/删除（索引归属权在 search-service），仅供服务间 Feign 调用。
 *
 * @author : Tomatos
 */
@FeignClient(name = "search-service", contextId = "internalSearchClient", path = "/internal/search")
public interface InternalSearchClient {

    /**
     * 增量写入单个搜索文档（不存在则新建，字段级更新不覆盖销量）。
     *
     * @param doc 搜索文档
     */
    @PostMapping("/upsert")
    void upsert(@RequestBody ProductDocResp doc);

    /**
     * 按商品 ID 删除搜索文档（幂等，文档不存在时忽略）。
     *
     * @param id 商品 ID
     */
    @PostMapping("/delete/{id}")
    void deleteById(@PathVariable("id") Long id);
}
