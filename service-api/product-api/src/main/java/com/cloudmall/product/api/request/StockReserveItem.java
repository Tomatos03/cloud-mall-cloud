package com.cloudmall.product.api.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存操作明细
 * <p>
 * 单个 SKU 的库存预占/释放数量。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReserveItem {

    /** SKU ID */
    @NotNull
    private Long skuId;

    /** 操作数量 */
    @Min(1)
    @NotNull
    private Integer quantity;
}
