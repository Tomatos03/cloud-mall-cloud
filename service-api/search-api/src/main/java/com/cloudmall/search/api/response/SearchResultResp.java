package com.cloudmall.search.api.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品搜索分页结果响应
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultResp {

    /** 当前页商品列表 */
    private List<SearchProductResp> items;

    /** 聚合筛选数据 */
    private SearchFacetsResp facets;

    /** 总命中数 */
    private Long total;

    /** 当前页码 */
    private Long page;

    /** 每页条数 */
    private Long pageSize;

    /** 总页数 */
    private Long pages;
}
