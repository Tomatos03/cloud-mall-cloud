package com.cloudmall.order.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 购物车操作请求
 *
 * @author : Tomatos
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true, fluent = true)
public class CartOperatorReq {

    /** SKU ID */
    private Long skuId;

    /** 数量 */
    private Integer quantity;
}