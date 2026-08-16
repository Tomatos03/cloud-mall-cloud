package com.cloudmall.search.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.search.api.request.SearchProductReq;
import com.cloudmall.search.api.response.SearchResultResp;
import com.cloudmall.search.service.ISearchService;

/**
 * 商品搜索 Controller
 * <p>
 * C 端搜索/浏览列表统一入口（网关路由 /search/**）。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final ISearchService searchService;

    /**
     * 搜索商品（关键词 + 筛选 + 分页 + 排序 + 聚合 + 高亮）。
     *
     * @param req 搜索请求
     * @return 搜索分页结果
     */
    @GetMapping("/products")
    public Result<SearchResultResp> search(@Valid SearchProductReq req) {
        return Result.success(searchService.search(req));
    }
}
