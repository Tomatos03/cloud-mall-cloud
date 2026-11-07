package com.cloudmall.coupon.api.model.resp;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优惠计算结果（子订单级）
 * <p>
 * 对应一个店铺子订单：原始金额、店铺券优惠、平台券优惠分摊和实付金额。
 * 子订单实付之和等于父订单商品应付金额。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponSubOrderResult {

    /** 店铺 ID */
    private Long shopId;

    /** 子订单原始金额 */
    private BigDecimal originalAmount;

    /** 店铺券优惠金额 */
    private BigDecimal shopDiscountAmount;

    /** 平台券优惠分摊金额 */
    private BigDecimal platformDiscountAmount;

    /** 子订单实付金额（不含运费） */
    private BigDecimal payableAmount;

    /** 明细级结果 */
    private List<CouponItemResult> items;
}
