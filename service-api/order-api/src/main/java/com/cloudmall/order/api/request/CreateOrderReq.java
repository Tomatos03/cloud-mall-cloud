package com.cloudmall.order.api.request;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建订单请求
 * <p>
 * 用户端只提交购物车选中 SKU、用户券选择、地址和备注。
 * userId、价格、商品名称、店铺、分类、优惠金额等一律由服务端从上下文与可信快照组装，
 * 不接受客户端提交。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderReq {

    /** 下单商品明细（购物车选中项） */
    @NotEmpty
    private List<@Valid CreateOrderItemReq> items;

    /** 平台券 userCouponId（可空，表示不使用平台券） */
    private Long platformUserCouponId;

    /** 店铺券选择：shopId → userCouponId（可空，表示对应店铺不使用店铺券） */
    private Map<Long, Long> shopUserCouponIds;

    /** 收货地址 ID */
    @NotNull
    private Long addressId;

    /** 订单备注 */
    private String remark;

    /**
     * 下单商品明细项
     * <p>
     * 只提交 SKU 与数量，价格等信息由服务端组装可信快照。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateOrderItemReq {

        /** SKU ID */
        @NotNull
        private Long skuId;

        /** 购买数量 */
        @NotNull
        private Integer quantity;
    }
}
