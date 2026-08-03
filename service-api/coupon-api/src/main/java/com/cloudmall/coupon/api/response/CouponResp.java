package com.cloudmall.coupon.api.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优惠券模板响应对象
 * <p>
 * 面向公开券中心与模板详情展示，包含发放窗口、有效期配置和
 * 当前用户领取状态（claimed），库存不足或窗口未开时 claimable 为 false。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponResp {

    /** 模板 ID */
    private Long id;

    /** 券展示名称 */
    private String name;

    /** 券归属（PLATFORM / SHOP） */
    private String ownerType;

    /** 店铺 ID（平台券为 null） */
    private Long shopId;

    /** 券类型（FULL_REDUCTION / DISCOUNT / CASH） */
    private String type;

    /** 适用范围（ALL / PRODUCT / CATEGORY） */
    private String scopeType;

    /** 使用门槛（0 表示无门槛） */
    private BigDecimal threshold;

    /** 满减/立减优惠金额 */
    private BigDecimal discountAmount;

    /** 折扣券减免比例（0.10 表示减免 10%） */
    private BigDecimal discountRate;

    /** 发行总量（null 表示不限量） */
    private Long totalCount;

    /** 累计发放数量 */
    private Long claimedCount;

    /** 是否允许公开领取 */
    private Boolean claimEnabled;

    /** 是否注册自动发放 */
    private Boolean autoIssueOnRegister;

    /** 有效期模式（FIXED / RELATIVE） */
    private String validityMode;

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

    /** 模板状态（DRAFT / ACTIVE / PAUSED / ENDED） */
    private String status;

    /** 首次发放时间 */
    private LocalDateTime firstIssuedTime;

    /** 当前用户是否已领取（匿名用户为 null） */
    private Boolean claimed;

    /** 当前是否可领取（窗口内、库存充足且未领取） */
    private Boolean claimable;

    /** 剩余可领取数量（null 表示不限量） */
    private Long stockRemaining;

    /** 指定商品 SPU ID 列表（scopeType=PRODUCT 时返回） */
    private List<Long> productIds;

    /** 指定分类 ID 列表（scopeType=CATEGORY 时返回） */
    private List<Long> categoryIds;

    /**
     * 是否已领完（存在总数限制且已领数达到总数）。
     *
     * @return true 表示已领完
     */
    public boolean isClaimedOut() {
        return this.totalCount != null && this.claimedCount != null
                       && this.claimedCount >= this.totalCount;
    }
}
