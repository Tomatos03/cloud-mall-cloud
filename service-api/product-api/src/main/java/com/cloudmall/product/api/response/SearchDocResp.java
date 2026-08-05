package com.cloudmall.product.api.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索文档响应
 * <p>
 * product-service 提供给 search-service 的商品搜索文档快照
 * （search-docs 接口契约），由 product-service 在查询时组装：
 * SPU 字段 + SKU 最低价 + 分类/品牌名称 + 主图 URL。
 * <p>
 * 注意：该文档不包含 sales（销量由 search-service 侧单独维护），
 * 同步方写入 ES 时必须使用字段级更新，避免覆盖已有销量。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchDocResp {

    /** 商品 ID */
    private Long id;

    /** 商品名称 */
    private String name;

    /** SPU 编码 */
    private String spuCode;

    /** 商品描述 */
    private String description;

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

    /** 商品状态（0=下架 1=上架） */
    private Integer status;

    /** SKU 最低价 */
    private BigDecimal priceMin;

    /** 主图 URL */
    private String mainImage;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
