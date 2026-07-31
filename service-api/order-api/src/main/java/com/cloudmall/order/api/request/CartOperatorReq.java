package com.cloudmall.order.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

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
    @NotNull(message = "SKU ID 不能为空")
    private Long skuId;

    /** 数量 */
    @NotNull(message = "数量不能为空")
    @Positive(message = "数量必须大于零")
    @Max(value = 99, message = "数量不能超过99")
    private Integer quantity;
}