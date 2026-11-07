package com.cloudmall.product.api.model.resp;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.mybatisplus.enums.StatusEnum;

/**
 * SKU 响应对象
 * <p>
 * 封装商品 SKU（库存量单位）信息，包含价格、库存及主图。
 *
 * @author : Tomatos
 * @date : 2026/9/8
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuResp {

    /** SKU ID */
    private Long skuId;

    /** SKU 编码 */
    private String skuCode;

    /** 所属商品 ID */
    private Long productId;

    /** 所属分类 ID */
    private Long categoryId;

    /** 所属店铺 ID */
    private Long shopId;

    /** 商品名称 */
    private String productName;

    /** 销售价格 */
    private BigDecimal price;

    /** 市场价（划线价） */
    private BigDecimal marketPrice;

    /** 库存数量 */
    private Integer stock;

    /** SKU 主图 URL */
    private String image;

    /** 商品上下架状态 */
    private StatusEnum status;

    /** 规格描述，如 "红色；XL"，按规格组排序拼接 */
    private String specDesc;
}
