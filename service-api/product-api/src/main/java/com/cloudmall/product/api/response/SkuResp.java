package com.cloudmall.product.api.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    /** 销售价格 */
    private BigDecimal price;

    /** 市场价（划线价） */
    private BigDecimal marketPrice;

    /** 库存数量 */
    private Integer stock;

    /** SKU 主图 URL */
    private String image;
}
