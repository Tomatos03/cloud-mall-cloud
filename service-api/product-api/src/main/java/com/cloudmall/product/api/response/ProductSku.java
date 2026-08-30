package com.cloudmall.product.api.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品详情 SKU 结构
 * <p>
 * 商品详情响应中 skus 数组的元素，包含 SKU 编码、规格值 ID 列表、售价、
 * 市场价与库存。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSku {

    /** SKU 编码 */
    private String skuCode;

    /** 规格值 ID 列表 */
    private List<Long> specValueIds;

    /** 销售价格 */
    private BigDecimal price;

    /** 市场价（划线价） */
    private BigDecimal marketPrice;

    /** 库存数量 */
    private Integer stock;
}
