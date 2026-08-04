package com.cloudmall.order.api.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下单商品明细项
 * <p>
 * 只提交 SKU 与数量，价格等信息由服务端组装可信快照。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemReq {

    /** SKU ID */
    @NotNull
    private Long skuId;

    /** 购买数量 */
    @NotNull
    private Integer quantity;
}
