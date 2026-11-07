package com.cloudmall.product.model.bo;

import java.math.BigDecimal;

/**
 * SKU 最低价查询结果行。
 *
 * @param productId SPU ID
 * @param price 该 SPU 下 SKU 最低价
 * @author : Tomatos
 */
public record ProductSkuPrice(Long productId, BigDecimal price) {
}
