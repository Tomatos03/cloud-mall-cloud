package com.cloudmall.search.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.product.api.response.ProductDocResp;
import com.cloudmall.search.index.ProductIndexRepository;

/**
 * 搜索索引内部协同接口
 * <p>
 * 仅供消费服务通过 Feign 调用，不加入网关对外路由。
 * ES 商品索引的写入/删除由本接口统一承接（索引归属权在 search-service）。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/search-index")
public class SearchIndexInternalController {

    private final ProductIndexRepository indexRepository;

    /**
     * 增量写入单个搜索文档（不存在则新建，字段级更新不覆盖销量）。
     *
     * @param doc 搜索文档
     * @return 处理结果
     */
    @PostMapping("/upsert")
    public Result<Void> upsert(
            @RequestBody
            ProductDocResp doc
    ) {
        indexRepository.upsert(doc);
        return Result.success();
    }

    /**
     * 按商品 ID 删除搜索文档（幂等，文档不存在时忽略）。
     *
     * @param id 商品 ID
     * @return 处理结果
     */
    @PostMapping("/delete/{id}")
    public Result<Void> deleteById(
            @PathVariable("id")
            Long id
    ) {
        indexRepository.deleteById(id);
        return Result.success();
    }
}
