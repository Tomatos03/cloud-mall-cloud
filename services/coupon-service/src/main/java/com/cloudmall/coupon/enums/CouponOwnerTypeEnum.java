package com.cloudmall.coupon.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券归属枚举
 * <p>
 * 对应 cm_coupon.owner_type 列的取值集合：平台券由运营创建并承担成本，
 * 店铺券由商家创建并承担成本。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum CouponOwnerTypeEnum {

    /** 平台券 — shop_id 为空，成本由平台承担 */
    PLATFORM("PLATFORM", "平台券"),

    /** 店铺券 — shop_id 必填，成本由商家承担 */
    SHOP("SHOP", "店铺券");

    /** 数据库存储值 */
    @EnumValue
    private final String code;

    /** 归属描述 */
    private final String desc;

    /**
     * 根据编码查找券归属。
     *
     * @param code 归属编码，如 "PLATFORM"
     * @return 匹配的券归属
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    public static CouponOwnerTypeEnum fromCode(String code) {
        return Arrays.stream(values())
                     .filter(t -> t.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException(
                         "Invalid coupon owner type: '" + code + "', valid values: " + Arrays.toString(values())
                     ));
    }
}
