package com.cloudmall.search.service;

import com.cloudmall.search.api.request.SearchProductReq;
import com.cloudmall.search.api.response.SearchResultResp;
import com.cloudmall.search.model.RebuildResultResp;

/**
 * 商品搜索服务接口
 * <p>
 * 聚合 C 端查询（基于 ES 全文检索 + 筛选 + 排序 + 聚合 + 高亮）
 * 与管理端全量重建（翻页拉取 product-service 文档写入 ES 并清理残留）。
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

    /**
     * 全量重建商品索引：翻页拉取 product-service 全量搜索文档写入 ES， 并清理 ES
     * 中已不存在的残留文档。 <p>
     * 使用字段级更新（doc 参数不含 sales），不会覆盖销量。
     *
     * @return 重建统计结果
     */
    RebuildResultResp rebuild();
}
