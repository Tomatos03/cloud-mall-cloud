package com.cloudmall.coupon.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户券流水来源枚举
 * <p>
 * 对应 cm_user_coupon_log.source 列的取值集合，标识触发状态变更的操作方。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum UserCouponLogSourceEnum {

    /** 用户 — 公开领取 */
    USER("USER", "用户"),

    /** 订单 — 锁券、释放 */
    ORDER("ORDER", "订单"),

    /** 支付 — 支付成功核销 */
    PAYMENT("PAYMENT", "支付"),

    /** 系统 — 过期任务、自动发放 */
    SYSTEM("SYSTEM", "系统"),

    /** 运营 — 紧急撤销 */
    OPERATOR("OPERATOR", "运营");

    /** 数据库存储值 */
    @EnumValue
    private final String code;

    /** 来源描述 */
    private final String desc;

    /**
     * 根据编码查找流水来源。
     *
     * @param code 来源编码，如 "PAYMENT"
     * @return 匹配的流水来源
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    public static UserCouponLogSourceEnum fromCode(String code) {
        return Arrays.stream(values())
                     .filter(t -> t.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException(
                         "Invalid user coupon log source: '" + code + "', valid values: " + Arrays.toString(values())
                     ));
    }
}
