package com.cloudmall.product.api.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.cloudmall.product.api.client.fallback.HomeCategoryClientFallbackFactory;
import com.cloudmall.product.api.model.resp.CategoryResp;

/**
 * 首页聚合专用分类 Feign 客户端
 * <p>
 * 仅暴露首页聚合所需的平铺分类列表，供 user-service 首页聚合使用；
 * 配合 {@code fallbackFactory} 实现首页局部降级，不影响其他调用方。
 *
 * @author : Tomatos
 */
@FeignClient(
        name = "product-service",
        contextId = "homeCategoryClient",
        path = "/categories",
        fallbackFactory = HomeCategoryClientFallbackFactory.class
)
public interface HomeCategoryClient {

    /**
     * 查询启用中的分类平铺列表（含层级关系，按排序值升序）。
     *
     * @return 分类信息列表
     */
    @GetMapping
    List<CategoryResp> list();
}
