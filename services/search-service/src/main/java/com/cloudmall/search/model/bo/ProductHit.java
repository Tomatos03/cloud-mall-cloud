package com.cloudmall.search.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ES 商品文档模型
 * <p>
 * 用于反序列化搜索命中的 ES 文档（含 sales 字段）。
 * 与 {@code ProductDocResp} 的区别：priceMin 为 double（ES 存储类型）、
 * 不含组装字段，仅保留列表页展示所需字段。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductHit {

    /** 商品 ID */
    private Long id;

    /** 商品名称 */
    private String name;

    /** 商品描述 */
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

    /** 商品状态（0=下架 1=上架） */
    private Integer status;
}
