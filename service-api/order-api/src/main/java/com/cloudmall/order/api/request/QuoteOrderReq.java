package com.cloudmall.order.api.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下单前价格预览请求
 * <p>
 * 只需提交商品 SKU、数量和可选的优惠券 ID 列表，
 * 服务端自动推荐最优优惠组合并返回价格明细。
 * 无任何副作用，不锁券、不预占库存。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteOrderReq {

    /** 下单商品明细 */
    @Valid
    @NotEmpty
    private List<QuoteOrderItemReq> items;

    /** 用户选择的优惠券 ID 列表（可空，不传时自动推荐最优组合） */
    private List<Long> userCouponIds;

    /**
     * 下单商品明细项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuoteOrderItemReq {

        /** SKU ID */
        private Long skuId;

        /** 购买数量 */
        private Integer quantity;
    }
}
