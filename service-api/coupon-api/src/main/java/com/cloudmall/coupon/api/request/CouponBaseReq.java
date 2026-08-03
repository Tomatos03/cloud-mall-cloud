package com.cloudmall.coupon.api.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import cn.hutool.core.util.BooleanUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import com.cloudmall.coupon.enums.CouponOwnerTypeEnum;
import com.cloudmall.coupon.enums.CouponScopeTypeEnum;
import com.cloudmall.coupon.enums.CouponTypeEnum;
import com.cloudmall.coupon.enums.ValidityModeEnum;

/**
 * 优惠券模板请求体
 * <p>
 * 创建与编辑共用的模板核心规则字段，CouponUpdateReq 继承本类复用字段。
 *
 * @author : Tomatos
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CouponBaseReq {

    /** 券展示名称 */
    @NotBlank
    private String name;

    /** 券归属（PLATFORM / SHOP） */
    @NotNull
    private CouponOwnerTypeEnum ownerType;

    /** 店铺 ID（店铺券必填，商家创建时忽略并以当前店铺为准） */
    private Long shopId;

    /** 券类型（FULL_REDUCTION / DISCOUNT / CASH） */
    @NotNull
    private CouponTypeEnum type;

    /** 适用范围（ALL / PRODUCT / CATEGORY） */
    @NotNull
    private CouponScopeTypeEnum scopeType;
    /** 使用门槛（0 表示无门槛） */
    private BigDecimal threshold;

    /** 满减/立减优惠金额 */
    private BigDecimal discountAmount;

    /** 折扣券减免比例（0.10 表示减免 10%） */
    private BigDecimal discountRate;

    /** 发行总量（null 表示不限量） */
    private Long totalCount;

    /** 是否允许公开领取 */
    private Boolean claimEnabled;

    /** 是否注册自动发放 */
    private Boolean autoIssueOnRegister;

    /** 有效期模式（FIXED / RELATIVE） */
    @NotNull
    private ValidityModeEnum validityMode;

    /** 发放窗口开始时间 */
    @NotNull
    private LocalDateTime issueStartTime;

    /** 发放窗口结束时间 */
    @NotNull
    private LocalDateTime issueEndTime;

    /** 固定有效期生效时间（FIXED 模式） */
    private LocalDateTime fixedStartTime;

    /** 固定有效期过期时间（FIXED 模式） */
    private LocalDateTime fixedExpireTime;

    /** 相对有效期天数（RELATIVE 模式） */
    private Integer validDays;

    /** 指定商品 SPU ID 列表（scopeType=PRODUCT 时必填） */
    private List<Long> productIds;

    /** 指定分类 ID 列表（scopeType=CATEGORY 时必填） */
    private List<Long> categoryIds;

    /**
     * 领取与自动发放是否同时开启。
     *
     * @return true 表示两个渠道都开启
     */
    public boolean isBothIssueChannelsEnabled() {
        return BooleanUtil.isTrue(this.claimEnabled) && BooleanUtil.isTrue(this.autoIssueOnRegister);
    }

    /**
     * 折扣金额是否在有效范围内（大于 0 且低于门槛）。
     *
     * @param threshold 门槛金额
     * @return true 表示金额有效
     */
    public boolean isDiscountAmountValid(BigDecimal threshold) {
        return this.discountAmount != null && this.discountAmount.compareTo(BigDecimal.ZERO) > 0
                       && this.discountAmount.compareTo(threshold) < 0;
    }

    /**
     * 现金抵扣金额是否为正数。
     *
     * @return true 表示金额有效
     */
    public boolean isDiscountAmountPositive() {
        return this.discountAmount != null && this.discountAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 折扣率是否在 (0, 1) 范围内。
     *
     * @return true 表示折扣率有效
     */
    public boolean isDiscountRateValid() {
        return this.discountRate != null && this.discountRate.compareTo(BigDecimal.ZERO) > 0
                       && this.discountRate.compareTo(BigDecimal.ONE) < 0;
    }

    /**
     * 领取时间区间是否合法（开始早于结束）。
     *
     * @return true 表示时间区间有效
     */
    public boolean isIssueTimeValid() {
        return this.issueStartTime != null && this.issueEndTime != null
                       && this.issueStartTime.isBefore(this.issueEndTime);
    }

    /**
     * 固定有效期区间是否合法（开始早于结束）。
     *
     * @return true 表示时间区间有效
     */
    public boolean isFixedTimeValid() {
        return this.fixedStartTime != null && this.fixedExpireTime != null
                       && this.fixedStartTime.isBefore(this.fixedExpireTime);
    }

    /**
     * 相对有效期天数是否为正数。
     *
     * @return true 表示天数有效
     */
    public boolean hasValidDays() {
        return this.validDays != null && this.validDays > 0;
    }
}
