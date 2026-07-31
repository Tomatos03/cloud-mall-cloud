package com.cloudmall.order.api.request;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 选中/取消选中购物车商品请求
 *
 * @author : Tomatos
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CartSelectReq {

    /** SKU ID */
    @NotNull(message = "SKU ID 不能为空")
    private Long skuId;

    /** 是否选中（true=选中，false=取消选中） */
    @NotNull(message = "是否选中不能为空")
    private Boolean selected;
}
