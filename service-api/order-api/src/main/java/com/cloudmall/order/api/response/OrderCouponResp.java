package com.cloudmall.order.api.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单优惠关联响应
 * <p>
 * 记录本次订单实际使用的用户券及其计算快照，不重新读取当前模板计算历史金额。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCouponResp {

    /** 用户券 ID */
    private Long userCouponId;

    /** 优惠券模板 ID */
    private Long couponId;

    /** 券归属 code */
    private String ownerType;

    /** 店铺 ID（平台券为 null） */
    private Long shopId;

    /** 券类型快照 code */
    private String typeSnapshot;

    /** 门槛快照 */
    private BigDecimal thresholdSnapshot;

    /** 金额优惠快照 */
    private BigDecimal discountAmountSnapshot;

    /** 比例优惠快照 */
    private BigDecimal discountRateSnapshot;

    /** 适用金额 */
    private BigDecimal applicableAmount;

    /** 实际优惠金额 */
    private BigDecimal discountAmount;
}
