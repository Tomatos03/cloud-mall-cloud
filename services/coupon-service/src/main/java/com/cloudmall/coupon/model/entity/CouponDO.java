package com.cloudmall.coupon.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.coupon.api.enums.CouponEnum;
import com.cloudmall.coupon.api.enums.CouponOwnerEnum;
import com.cloudmall.coupon.api.enums.CouponScopeEnum;
import com.cloudmall.coupon.api.enums.CouponStatusEnum;
import com.cloudmall.coupon.api.enums.IssueChannelEnum;
import com.cloudmall.coupon.api.enums.ValidityModeEnum;

/**
 * 优惠券模板实体
 * <p>
 * 对应 cm_coupon 表，语义为模板而非用户持有的券资产。
 * 首次发布后核心规则冻结；已发放模板只能暂停或结束，不能删除。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_coupon")
public class CouponDO {

    /** 模板 ID */
    private Long id;

    /** 券展示名称 */
    private String name;

    /** 券归属（PLATFORM / SHOP） */
    private CouponOwnerEnum ownerType;

    /** 店铺 ID（平台券为 null） */
    private Long shopId;

    /** 创建人 ID（仅审计） */
    private Long createdBy;

    /** 券类型 */
    private CouponEnum type;

    /** 适用范围 */
    private CouponScopeEnum scopeType;

    /** 使用门槛（0 表示无门槛） */
    private BigDecimal threshold;

    /** 满减/立减优惠金额 */
    private BigDecimal discountAmount;

    /** 折扣券减免比例（0.10 表示减免 10%） */
    private BigDecimal discountRate;

    /** 发行总量（null 表示不限量） */
    private Long totalCount;

    /** 累计发放数量（只增不减） */
    private Long claimedCount;

    /** 发放渠道（PUBLIC_CLAIM=公开领取 REGISTER_AUTO=注册自动发放，null=未设置不可发布） */
    private IssueChannelEnum issueChannel;

    /** 有效期模式 */
    private ValidityModeEnum validityMode;

    /** 发放窗口开始时间 */
    private LocalDateTime issueStartTime;

    /** 发放窗口结束时间 */
    private LocalDateTime issueEndTime;

    /** 固定有效期生效时间 */
    private LocalDateTime fixedStartTime;

    /** 固定有效期过期时间 */
    private LocalDateTime fixedExpireTime;

    /** 相对有效期天数 */
    private Integer validDays;

    /** 模板状态 */
    private CouponStatusEnum status;

    /** 首次发放时间 */
    private LocalDateTime firstIssuedTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 是否删除（0=否 1=是） */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Boolean deleted;

    /**
     * 状态是否为启用或暂停（可结束发放）。
     *
     * @return true 表示可结束
     */
    public boolean isActiveOrPaused() {
        return CouponStatusEnum.ACTIVE.equals(this.status) || CouponStatusEnum.PAUSED.equals(this.status);
    }

    /**
     * 是否已设置发放渠道。
     *
     * @return true 表示可发放
     */
    public boolean isClaimable() {
        return this.issueChannel != null;
    }

    /**
     * 发放渠道是否为公开领取。
     *
     * @return true 表示公开领取渠道
     */
    public boolean isPublicClaimChannel() {
        return IssueChannelEnum.PUBLIC_CLAIM.equals(this.issueChannel);
    }

    /**
     * 发放渠道是否为注册自动发放。
     *
     * @return true 表示注册自动发放渠道
     */
    public boolean isRegisterAutoChannel() {
        return IssueChannelEnum.REGISTER_AUTO.equals(this.issueChannel);
    }

    /**
     * 发放是否尚未开始。
     *
     * @param now 当前时间
     * @return true 表示未开始
     */
    public boolean isNotStarted(LocalDateTime now) {
        return this.issueStartTime != null && this.issueStartTime.isAfter(now);
    }

    /**
     * 发放是否已结束。
     *
     * @param now 当前时间
     * @return true 表示已结束
     */
    public boolean isEnded(LocalDateTime now) {
        return this.issueEndTime != null && this.issueEndTime.isBefore(now);
    }

    /**
     * 库存是否充足（已领取数未达总数）。
     *
     * @return true 表示有库存
     */
    public boolean isStockAvailable() {
        return this.claimedCount != null && this.claimedCount < this.totalCount;
    }

    /**
     * 归属是否为平台券。
     *
     * @return true 表示平台券
     */
    public boolean isPlatform() {
        return CouponOwnerEnum.PLATFORM.equals(this.ownerType);
    }

    /**
     * 归属是否为店铺券。
     *
     * @return true 表示店铺券
     */
    public boolean isShop() {
        return CouponOwnerEnum.SHOP.equals(this.ownerType);
    }
}
