package com.cloudmall.coupon.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券有效期模式枚举
 * <p>
 * 对应 cm_coupon.validity_mode 列的取值集合：固定有效期对所有用户统一生效，
 * 相对有效期从发放时刻起按天数计算，模板停止发放不截断已发放用户券。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum ValidityModeEnum {
    /** 固定有效期 — 使用 fixed_start_time / fixed_expire_time */
    FIXED("FIXED", "固定有效期"),

    /** 发放后相对有效期 — 使用 valid_days */
    RELATIVE("RELATIVE", "发放后相对有效期");
    /** 数据库存储值 */
    @EnumValue
    @JsonValue
    private final String code;
    /** 模式描述 */
    private final String desc;
    /**
     * 根据编码查找有效期模式。
     *
     * @param code 模式编码，如 "FIXED"
     * @return 匹配的有效期模式
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    @JsonCreator
    public static ValidityModeEnum fromCode(@JsonProperty("code") String code) {
        return Arrays
                .stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid validity mode: '" + code + "', valid values: "
                                        + Arrays.toString(values())
                        )
                );
    }
}
