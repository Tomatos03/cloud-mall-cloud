package com.cloudmall.coupon.api.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.coupon.enums.CouponEnum;
import com.cloudmall.coupon.enums.CouponOwnerEnum;
import com.cloudmall.coupon.enums.CouponScopeEnum;

/**
 * 合法候选券
 * <p>
 * 试算时返回用户所有合法候选券（范围命中、状态可用），
 * valid = false 表示候选但当前不满足门槛或状态不允许。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponCandidateResp {

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

    /** 适用范围 */
    private CouponScopeEnum scopeType;

    /** 使用门槛 */
    private BigDecimal threshold;

    /** 满减/立减优惠金额 */
    private BigDecimal discountAmount;

    /** 折扣比例 */
    private BigDecimal discountRate;

    /** 生效时间 */
    private LocalDateTime effectiveTime;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 预计适用金额 */
    private BigDecimal applicableAmount;

    /** 预计优惠金额 */
    private BigDecimal estimatedDiscountAmount;

    /** 是否可用（范围命中且满足门槛且状态可用） */
    private Boolean valid;

    /** 不可用原因（valid = false 时给出） */
    private String invalidReason;
}
