package com.cloudmall.coupon.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.coupon.enums.CouponOwnerTypeEnum;
import com.cloudmall.coupon.enums.CouponScopeTypeEnum;
import com.cloudmall.coupon.enums.CouponTypeEnum;
import com.cloudmall.coupon.enums.IssuedSourceEnum;
import com.cloudmall.coupon.enums.UserCouponStatusEnum;

/**
 * 用户券实体
 * <p>
 * 对应 cm_user_coupon 表，表示用户实际持有的优惠权益，
 * 保存领取时的核心快照，快照创建后不可修改。
 * 唯一索引 (user_id, coupon_id) 保证同一用户同一模板只有一张用户券。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_user_coupon")
public class UserCouponDO {

    /** 用户券 ID */
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 模板 ID */
    private Long couponId;

    /** 领取时归属快照 */
    private CouponOwnerTypeEnum ownerType;

    /** 店铺券归属快照 */
    private Long shopId;

    /** 名称快照 */
    private String nameSnapshot;

    /** 券类型快照 */
    private CouponTypeEnum typeSnapshot;

    /** 适用范围快照 */
    private CouponScopeTypeEnum scopeTypeSnapshot;

    /** 门槛快照 */
    private BigDecimal thresholdSnapshot;

    /** 金额优惠快照 */
    private BigDecimal discountAmountSnapshot;

    /** 比例优惠快照 */
    private BigDecimal discountRateSnapshot;

    /** 实际生效时间 */
    private LocalDateTime effectiveTime;

    /** 实际过期时间 */
    private LocalDateTime expireTime;

    /** 用户券状态 */
    private UserCouponStatusEnum status;

    /** 当前锁定订单号（非永久历史） */
    private String lockedOrderNo;

    /** 当前锁定过期时间 */
    private LocalDateTime lockExpireTime;

    /** 当前状态变更时间 */
    private LocalDateTime stateTime;

    /** 发放来源 */
    private IssuedSourceEnum issuedSource;

    /** 自动发放事件 ID（公开领取为空） */
    private String issuedEventId;

    /** 发放时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 是否删除（0=否 1=是） */
    @TableLogic
    private Boolean deleted;

    /**
     * 是否已过期（状态可用且到期时间已过）。
     *
     * @return true 表示已过期
     */
    public boolean isOverdue() {
        return UserCouponStatusEnum.AVAILABLE.equals(this.status) && this.expireTime != null
                       && this.expireTime.isBefore(LocalDateTime.now());
    }

    /**
     * 状态是否为可用或已过期（回退冲突检查用）。
     *
     * @return true 表示可用或已过期
     */
    public boolean isAvailableOrExpired() {
        return UserCouponStatusEnum.AVAILABLE.equals(this.status) || UserCouponStatusEnum.EXPIRED.equals(this.status);
    }
}
