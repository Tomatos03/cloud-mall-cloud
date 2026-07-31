package com.cloudmall.product.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cloudmall.common.model.Result;
import com.cloudmall.product.api.response.CategoryResp;

/**
 * 商品分类 Feign 客户端。
 *
 * @author : Tomatos
 */
@FeignClient(name = "product-service", contextId = "categoryClient", path = "/categories")
public interface CategoryClient {

    /**
     * 根据 ID 查询分类。
     *
     * @param id 分类 ID
     * @return 分类信息
     */
    @GetMapping("/{id}")
    Result<CategoryResp> getById(@PathVariable("id") Long id);
}
