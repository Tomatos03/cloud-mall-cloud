package com.cloudmall.coupon.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 我的优惠券可用性查询分组
 * <p>
 * 仅用于用户端“我的优惠券”游标分页的状态过滤，不是数据库状态：
 * AVAILABLE 表示未过期且满足使用条件，UNAVAILABLE 表示未过期但不满足使用条件。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum UserCouponUsableEnum {
    /** 可用 — 未过期且范围命中、满足门槛 */
    AVAILABLE("AVAILABLE", "可用"),

    /** 不可用 — 未过期但不满足使用条件 */
    UNAVAILABLE("UNAVAILABLE", "不可用");
    /** 查询值 */
    private final String code;
    /** 描述 */
    private final String desc;
}
