package com.cloudmall.coupon.api.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优惠券统计响应
 * <p>
 * 模板维度基于用户券流水聚合，成本维度基于订单优惠快照聚合。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponStatsResp {

    /** 模板 ID（全局概览为 null） */
    private Long couponId;

    /** 模板名称（全局概览为 null） */
    private String name;

    /** 发行量（含注册自动发放） */
    private Long issueCount;

    /** 锁定量 */
    private Long lockedCount;

    /** 核销量 */
    private Long committedCount;

    /** 释放量 */
    private Long releasedCount;

    /** 回退量 */
    private Long revertedCount;

    /** 过期量 */
    private Long expiredCount;

    /** 撤销量 */
    private Long revokedCount;

    /** 使用订单数 */
    private Long orderCount;

    /** 补贴金额（该模板在订单中的实际优惠合计） */
    private BigDecimal subsidyAmount;
}
