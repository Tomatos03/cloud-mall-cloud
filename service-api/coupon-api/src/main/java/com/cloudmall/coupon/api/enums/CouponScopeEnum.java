package com.cloudmall.coupon.api.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券适用范围枚举
 * <p>
 * 对应 cm_coupon.scope_type 列的取值集合，单一范围类型，
 * 同类型目标按 OR 匹配，不支持混合和排除。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum CouponScopeEnum {
    /** 全范围 — 归属范围内所有符合条件商品，不产生范围关联记录 */
    ALL("ALL", "全范围"),

    /** 指定商品 — 按 SPU 配置，覆盖该 SPU 下全部 SKU */
    PRODUCT("PRODUCT", "指定商品"),

    /** 指定分类 — 命中目标分类或其后代分类 */
    CATEGORY("CATEGORY", "指定分类");
    /** 数据库存储值 */
    @EnumValue
    @JsonValue
    private final String code;
    /** 范围描述 */
    private final String desc;
    /**
     * 根据编码查找适用范围。
     *
     * @param code 范围编码，如 "PRODUCT"
     * @return 匹配的适用范围
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    @JsonCreator
    public static CouponScopeEnum fromCode(@JsonProperty("code") String code) {
        return Arrays
                .stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid coupon scope type: '" + code + "', valid values: "
                                        + Arrays.toString(values())
                        )
                );
    }
}
