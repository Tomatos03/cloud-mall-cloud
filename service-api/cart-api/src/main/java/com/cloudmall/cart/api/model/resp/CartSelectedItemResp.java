package com.cloudmall.cart.api.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 已选中购物车项契约
 * <p>
 * 供订单结算通过 Feign 读取购物车已选中项；只暴露下单需要的 SKU 与数量，
 * 不暴露购物车持久化对象与缓存状态。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSelectedItemResp {

    /** SKU ID */
    private Long skuId;

    /** 下单数量 */
    private Integer quantity;
}
