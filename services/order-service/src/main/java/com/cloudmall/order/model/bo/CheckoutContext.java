package com.cloudmall.order.model.bo;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.order.api.enums.CheckoutTypeEnum;
import com.cloudmall.order.api.model.req.CreateOrderReq;
import com.cloudmall.order.model.entity.OrderDO;
import com.cloudmall.product.api.model.resp.SkuResp;
import com.cloudmall.user.api.model.resp.AddressResp;

/**
 * 下单上下文：承载下单主流程各阶段共享的中间数据。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutContext {

    /** 当前登录用户 ID */
    private Long userId;

    /** 结算类型（策略分发依据，落库 order_type） */
    private CheckoutTypeEnum checkoutType;

    /** 下单请求 */
    private CreateOrderReq req;

    /** 收货地址 */
    private AddressResp address;

    /** 结算商品项：key 为 SKU ID，value 为该 SKU 的下单商品数量 */
    private Map<Long, Integer> checkoutItems;

    /** SKU 可信快照：key 为 SKU ID，value 为对应 SKU 快照 */
    private Map<Long, SkuResp> skuMap;

    /** 最终父订单（落库步骤写入） */
    private OrderDO order;
}
