package com.cloudmall.product.api.client;

import java.time.LocalDateTime;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cloudmall.product.api.response.SearchDocPageResp;

/**
 * 搜索文档 Feign 客户端
 * <p>
 * search-service 通过该客户端拉取商品搜索文档（全量重建/定时增量同步的数据源）。
 * 文档组装逻辑唯一属于 product-service，search-service 只负责写入 ES。
 *
 * @author : Tomatos
 */
@FeignClient(name = "product-service", contextId = "searchDocClient", path = "/product")
public interface SearchDocClient {

    /**
     * 分页拉取搜索文档。 <p>
     * 按 updateTime 升序排列，支持 since 增量游标（仅返回 updateTime 晚于
     * since 的商品）。
     *
     * @param page 页码，从 1 开始
     * @param size 每页条数
     * @param since 增量游标（可选），ISO 日期时间
     * @return 搜索文档分页结果
     */
    @GetMapping("/search-docs")
    SearchDocPageResp searchDocs(
            @RequestParam("page")
            long page,
            @RequestParam("size")
            long size,
            @RequestParam(value = "since", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime since
    );
}
