package com.cloudmall.product.api.client;

import java.util.List;
import java.util.Set;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cloudmall.product.api.client.fallback.HomeProductClientFallbackFactory;
import com.cloudmall.product.api.response.ProductDocResp;

/**
 * 首页聚合专用商品 Feign 客户端
 * <p>
 * 仅暴露首页聚合所需的分类商品查询，供 user-service 首页聚合使用；
 * 配合 {@code fallbackFactory} 实现首页局部降级，不影响其他调用方。
 *
 * @author : Tomatos
 */
@FeignClient(
        name = "product-service",
        contextId = "homeProductClient",
        path = "/products",
        fallbackFactory = HomeProductClientFallbackFactory.class
)
public interface HomeProductClient {

    /**
     * 按分类 ID 集合查询已上架商品（首页分类展示用）。
     *
     * @param categoryIds 分类 ID 集合（可包含指定分类及其子分类）
     * @param limit 查询条数上限（1~50，默认 10）
     * @return 商品列表
     */
    @GetMapping
    List<ProductDocResp> listByCategory(
        @RequestParam("categoryIds")
        Set<Long> categoryIds,
        @RequestParam(value = "limit", defaultValue = "10")
        Integer limit
    );
}
