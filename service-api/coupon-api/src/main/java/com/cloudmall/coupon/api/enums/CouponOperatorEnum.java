package com.cloudmall.coupon.api.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模板操作人类型枚举
 * <p>
 * 对应 cm_coupon_operation_log.operator_type 列的取值集合，
 * 区分运营端与商家端的模板管理操作。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum CouponOperatorEnum {
    /** 运营 */
    OPERATOR("OPERATOR", "运营"),

    /** 商家 */
    MERCHANT("MERCHANT", "商家");
    /** 数据库存储值 */
    @EnumValue
    @JsonValue
    private final String code;
    /** 类型描述 */
    private final String desc;
    /**
     * 根据编码查找操作人类型。
     *
     * @param code 类型编码，如 "MERCHANT"
     * @return 匹配的操作人类型
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    @JsonCreator
    public static CouponOperatorEnum fromCode(@JsonProperty("code") String code) {
        return Arrays
                .stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid coupon operator type: '" + code + "', valid values: "
                                        + Arrays.toString(values())
                        )
                );
    }
}
