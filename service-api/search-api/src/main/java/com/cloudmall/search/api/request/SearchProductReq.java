package com.cloudmall.search.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品搜索请求
 * <p>
 * C 端搜索/浏览列表的统一查询参数：
 * 关键词 + 分类/品牌/价格/店铺筛选 + 分页 + 排序。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchProductReq {

    /** 关键词（商品名/描述/SPU/品牌/分类名匹配） */
    private String keyword;

    /** 分类 ID */
    private Long categoryId;

    /** 品牌 ID */
    private Long brandId;

    /** 最低价（含） */
    private Double priceMin;

    /** 最高价（含） */
    private Double priceMax;

    /** 店铺 ID */
    private Long shopId;

    /** 页码，从 1 开始 */
    @Min(1)
    @Builder.Default
    private Integer page = 1;

    /** 每页条数 */
    @Min(1)
    @Max(100)
    @Builder.Default
    private Integer pageSize = 20;

    /**
     * 排序方式：
     * relevance（默认，相关度）、price_asc、price_desc、sales、newest。
     */
    private String sortBy;
}
