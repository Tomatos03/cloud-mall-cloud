package com.cloudmall.coupon.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券类型枚举
 * <p>
 * 对应 cm_coupon.type 列的取值集合，模板发布后冻结，用户券保存领取时快照。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum CouponTypeEnum {
    /** 满减券 — 满足门槛后按固定金额减免 */
    FULL_REDUCTION("FULL_REDUCTION", "满减"),

    /** 折扣券 — 按减免比例优惠，discount_rate 表示减免比例（0.10 表示减免 10%） */
    DISCOUNT("DISCOUNT", "折扣"),

    /** 立减券 — 无门槛或满足门槛后按固定金额直接立减 */
    CASH("CASH", "立减");
    /** 数据库存储值 */
    @EnumValue
    private final String code;
    /** 类型描述 */
    private final String desc;
    /**
     * 根据编码查找优惠券类型。
     *
     * @param code 类型编码，如 "FULL_REDUCTION"
     * @return 匹配的优惠券类型
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    public static CouponTypeEnum fromCode(String code) {
        return Arrays
                .stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid coupon type: '" + code + "', valid values: "
                                        + Arrays.toString(values())
                        )
                );
    }
}
