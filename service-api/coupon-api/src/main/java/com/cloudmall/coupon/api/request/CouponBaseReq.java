package com.cloudmall.coupon.api.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 优惠券模板请求基类
 * <p>
 * 包含创建与编辑共用的模板核心规则字段。
 *
 * @author : Tomatos
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class CouponBaseReq {

    /** 券展示名称 */
    @NotBlank
    private String name;

    /** 券类型（FULL_REDUCTION / DISCOUNT / CASH） */
    @NotBlank
    private String type;

    /** 适用范围（ALL / PRODUCT / CATEGORY） */
    @NotBlank
    private String scopeType;

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
    @NotBlank
    private String validityMode;

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
}
