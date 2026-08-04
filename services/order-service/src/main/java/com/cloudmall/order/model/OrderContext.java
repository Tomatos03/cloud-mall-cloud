package com.cloudmall.order.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.coupon.api.dto.CouponCalcItem;
import com.cloudmall.order.api.request.CreateOrderReq;
import com.cloudmall.product.api.response.SkuResp;
import com.cloudmall.user.api.response.AddressResp;

/**
 * 下单上下文：承载下单主流程各阶段共享的中间数据。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderContext {

    /** 当前登录用户 ID */
    private Long userId;

    /** 下单请求 */
    private CreateOrderReq req;

    /** 收货地址 */
    private AddressResp address;

    /** skuId → 数量 */
    private Map<Long, Integer> quantities;

    /** SKU 可信快照 */
    private Map<Long, SkuResp> skuMap;

    /** 可信商品明细 */
    private List<CouponCalcItem> calcItems;

    /** 参与店铺（保序） */
    private List<Long> shopIds;
}
