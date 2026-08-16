package com.cloudmall.product.api.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cloudmall.product.api.client.fallback.BannerClientFallbackFactory;
import com.cloudmall.product.api.response.BannerResp;

/**
 * 轮播图 Feign 客户端。
 * <p>
 * 复用既有分页端点 {@code GET /banners/page}（默认仅查询已启用、按排序值升序），
 * 供首页聚合等场景分页拉取。
 *
 * @author : Tomatos
 */
@FeignClient(
        name = "product-service",
        contextId = "bannerClient",
        path = "/banners",
        fallbackFactory = BannerClientFallbackFactory.class
)
public interface BannerClient {

    /**
     * 分页查询轮播图。
     *
     * @param current 当前页码
     * @param size 每页条数
     * @return 轮播图分页结果
     */
    @GetMapping("/page")
    Page<BannerResp> page(
            @RequestParam(value = "current", defaultValue = "1")
            long current,
            @RequestParam(value = "size", defaultValue = "10")
            long size
    );
}
