package com.cloudmall.search.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.product.api.response.ProductDocResp;

/**
 * 搜索索引协同 Feign 客户端
 * <p>
 * 内部接口，仅供消费服务通过 Feign 调用；不加入网关对外路由。
 * ES 索引的写入/删除归属 search-service，本客户端把商品事件处理
 * 落到 search-service 完成索引更新。
 *
 * @author : Tomatos
 */
@FeignClient(name = "search-service", contextId = "searchIndexClient", path = "/internal/search-index")
public interface SearchIndexClient {

    /**
     * 增量写入单个搜索文档（不存在则新建，字段级更新不覆盖销量）。
     *
     * @param doc 搜索文档
     */
    @PostMapping("/upsert")
    void upsert(
            @RequestBody
            ProductDocResp doc
    );

    /**
     * 按商品 ID 删除搜索文档（幂等，文档不存在时忽略）。
     *
     * @param id 商品 ID
     */
    @PostMapping("/delete/{id}")
    void deleteById(
            @PathVariable("id")
            Long id
    );
}
