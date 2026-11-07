package com.cloudmall.coupon.api.model.resp;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优惠计算结果（明细级）
 * <p>
 * 记录单条商品明细的原始金额、店铺/平台优惠分摊和实付金额。
 * 明细实付之和等于所属子订单实付。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponItemResult {

    /** SKU ID */
    private Long skuId;

    /** SPU ID */
    private Long productId;

    /** 原始金额（unitPrice × quantity） */
    private BigDecimal originalAmount;

    /** 店铺券优惠分摊 */
    private BigDecimal shopDiscountAmount;

    /** 平台券优惠分摊 */
    private BigDecimal platformDiscountAmount;

    /** 明细实付金额 */
    private BigDecimal payableAmount;
}
