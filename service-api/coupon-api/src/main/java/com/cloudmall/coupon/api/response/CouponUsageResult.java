package com.cloudmall.coupon.api.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.coupon.enums.CouponEnum;
import com.cloudmall.coupon.enums.CouponOwnerEnum;

/**
 * 实际使用的券快照
 * <p>
 * 记录订单实际使用的用户券及其计算快照，不重新读取当前模板计算历史金额。
 * 平台券关联父订单，店铺券关联对应店铺子订单。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponUsageResult {

    /** 用户券 ID */
    private Long userCouponId;

    /** 模板 ID */
    private Long couponId;

    /** 券归属 */
    private CouponOwnerEnum ownerType;

    /** 店铺 ID（平台券为 null） */
    private Long shopId;

    /** 券名称 */
    private String name;

    /** 券类型 */
    private CouponEnum type;

    /** 门槛快照（0 表示无门槛） */
    private BigDecimal thresholdSnapshot;

    /** 金额优惠快照 */
    private BigDecimal discountAmountSnapshot;

    /** 比例优惠快照 */
    private BigDecimal discountRateSnapshot;

    /** 适用金额 */
    private BigDecimal applicableAmount;

    /** 优惠金额 */
    private BigDecimal discountAmount;
}
