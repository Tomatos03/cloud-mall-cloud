package com.cloudmall.order.api.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 店铺子订单响应
 * <p>
 * 商家查询和操作自己的子订单，用户通过父订单查看。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSubResp {

    /** 子订单 ID */
    private Long id;

    /** 子订单编号 */
    private String subOrderNo;

    /** 父订单 ID */
    private Long parentOrderId;

    /** 店铺 ID */
    private Long shopId;

    /** 商品原价金额 */
    private BigDecimal originalAmount;

    /** 店铺券优惠金额 */
    private BigDecimal shopDiscountAmount;

    /** 平台券优惠金额 */
    private BigDecimal platformDiscountAmount;

    /** 商品应付金额 */
    private BigDecimal payableAmount;

    /** 商家结算金额 */
    private BigDecimal merchantSettlementAmount;

    /** 子订单状态 code */
    private String status;

    /** 明细列表 */
    private List<OrderItemResp> items;
}
