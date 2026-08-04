package com.cloudmall.order.api.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.order.api.model.OrderItemReq;

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
    @Valid
    @NotEmpty
    private List<OrderItemReq> items;

    /** 用户选择的优惠券 ID 列表（可空，不传时不使用优惠券） */
    private List<Long> userCouponIds;

    /** 收货地址 ID */
    @NotNull
    private Long addressId;

    /** 订单备注 */
    private String remark;
}
