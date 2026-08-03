package com.cloudmall.order.api.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优惠券订单使用统计响应
 * <p>
 * 基于订单优惠关联快照聚合：使用订单数与实际补贴金额。
 * 由 order-service 内部统计接口提供，供 coupon-service 成本统计使用。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponUsageStatsResp {

    /** 使用订单数 */
    private Long orderCount;

    /** 补贴金额（该模板在订单中的实际优惠合计） */
    private BigDecimal subsidyAmount;
}
