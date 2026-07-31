package com.cloudmall.product.api.response;

import java.util.List;

import com.cloudmall.mybatisplus.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品详情响应对象
 * <p>
 * 封装商品完整详情，包含品牌、图片列表、SKU 列表、规格分组等。
 *
 * @author : Tomatos
 * @date : 2026/9/8
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResp {

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

    /** 品牌信息 */
    private BrandResp brand;

    /** 商品图片列表 */
    private List<ImageResp> images;

    /** SKU 列表 */
    private List<SkuResp> skus;

    /** 规格分组列表 */
    private List<SpecGroupResp> specGroups;
}
