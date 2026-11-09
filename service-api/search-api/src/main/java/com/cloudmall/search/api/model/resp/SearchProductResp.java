package com.cloudmall.search.api.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品搜索结果响应
 * <p>
 * 直接来源于 ES 文档，自包含列表页所需展示字段。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchProductResp {

    /** 商品 ID */
    private Long id;

    /** 商品名称 */
    private String name;

    /** 商品描述（列表页摘要展示） */
    private String description;

    /** SPU 编码 */
    private String spuCode;

    /** 主图 URL */
    private String mainImage;

    /** SKU 最低价 */
    private Double priceMin;

    /** 累计销量 */
    private Long sales;

    /** 分类 ID */
    private Long categoryId;

    /** 分类名称 */
    private String categoryName;

    /** 品牌 ID */
    private Long brandId;

    /** 品牌名称 */
    private String brandName;

    /** 所属店铺 ID */
    private Long shopId;

    /** 高亮后的商品名称（含 &lt;em&gt; 标签），无命中时与 name 一致 */
    private String highlightName;
}
