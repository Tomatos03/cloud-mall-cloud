package com.cloudmall.search.service;

import com.cloudmall.search.api.request.SearchProductReq;
import com.cloudmall.search.api.response.SearchResultResp;

/**
 * 商品搜索服务接口
 * <p>
 * 基于 ES 全文检索 + 筛选 + 排序 + 聚合 + 高亮。
 *
 * @author : Tomatos
 */
public interface ISearchService {

    /**
     * 搜索商品。
     *
     * @param req 搜索请求（关键词/筛选/分页/排序）
     * @return 搜索分页结果（含聚合筛选项与高亮）
     */
    SearchResultResp search(SearchProductReq req);
}
