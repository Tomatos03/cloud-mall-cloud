package com.cloudmall.coupon.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户券状态枚举
 * <p>
 * 对应 cm_user_coupon.status 列的取值集合。
 * USED、EXPIRED、REVOKED 为终态，USED 仅能由全额退款流程回退。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum UserCouponStatusEnum {
    /** 可用 — 可参与试算和锁券 */
    AVAILABLE("AVAILABLE", "可用"),

    /** 锁定 — 已被订单锁定，只能绑定一个未结束订单 */
    LOCKED("LOCKED", "锁定"),

    /** 已使用 — 支付成功核销，全额退款且未过期时回退 */
    USED("USED", "已使用"),

    /** 已过期 — 超过 expire_time */
    EXPIRED("EXPIRED", "已过期"),

    /** 已撤销 — 运营紧急撤销 */
    REVOKED("REVOKED", "已撤销");
    /** 数据库存储值 */
    @EnumValue
    private final String code;
    /** 状态描述 */
    private final String desc;
    /**
     * 根据编码查找用户券状态。
     *
     * @param code 状态编码，如 "AVAILABLE"
     * @return 匹配的用户券状态
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    public static UserCouponStatusEnum fromCode(String code) {
        return Arrays
                .stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid user coupon status: '" + code + "', valid values: "
                                        + Arrays.toString(values())
                        )
                );
    }
}
