package com.cloudmall.product.api.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.product.api.model.resp.CategoryResp;

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
    CategoryResp getById(@PathVariable("id") Long id);

    /**
     * 批量查询分类（分类后代匹配用）。
     *
     * @param ids 分类 ID 列表
     * @return 分类信息列表
     */
    @PostMapping("/batch")
    List<CategoryResp> getByIds(@RequestBody List<Long> ids);
}
