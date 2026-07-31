package com.cloudmall.coupon.api.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户券响应对象
 * <p>
 * 展示用户实际持有的优惠权益快照，快照创建后不可修改。
 *
 * @author : Tomatos
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserCouponResp {

    /** 用户券 ID */
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 模板 ID */
    private Long couponId;

    /** 领取时归属快照（PLATFORM / SHOP） */
    private String ownerType;

    /** 店铺券归属快照 */
    private Long shopId;

    /** 名称快照 */
    private String nameSnapshot;

    /** 券类型快照 */
    private String typeSnapshot;

    /** 适用范围快照 */
    private String scopeTypeSnapshot;

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

    /** 用户券状态（AVAILABLE / LOCKED / USED / EXPIRED / REVOKED） */
    private String status;

    /** 当前锁定订单号 */
    private String lockedOrderNo;

    /** 发放来源（PUBLIC_CLAIM / REGISTER_AUTO） */
    private String issuedSource;

    /** 发放时间 */
    private LocalDateTime createTime;
}
