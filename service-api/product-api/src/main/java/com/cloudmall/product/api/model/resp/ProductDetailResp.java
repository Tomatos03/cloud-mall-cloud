package com.cloudmall.product.api.model.resp;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.user.api.model.resp.ShopInfoResp;

/**
 * 商品详情响应对象
 * <p>
 * 封装商品完整详情，包含品牌、图片列表、SKU 列表、规格分组，以及聚合的
 * 店铺信息与评论摘要，一次请求即可获取详情页全部数据。
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

    /** 店铺 ID */
    private Long shopId;

    /** 品牌信息 */
    private BrandResp brand;

    /** 商品图片列表 */
    private List<ImageResp> images;

    /** SKU 列表 */
    private List<ProductSku> skus;

    /** 规格分组列表 */
    private List<SpecGroupResp> specGroups;

    /** 店铺信息（含评分、商品数、好评率、关注数） */
    private ShopInfoResp shop;

    /** 评论摘要（综合评分、星级分布、三档计数等） */
    private CommentSummaryResp commentSummary;
}
