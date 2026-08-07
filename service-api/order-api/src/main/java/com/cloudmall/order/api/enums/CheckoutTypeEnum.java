package com.cloudmall.order.api.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 结算类型枚举
 * <p>
 * 对应 cm_order.order_type 列，同时是统一建单接口路径参数的分发依据：
 * CART 为购物车结算，DIRECT 为直接购买，SECKILL 为秒杀建单。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum CheckoutTypeEnum {
    /** 购物车结算 */
    CART("CART", "购物车结算"),

    /** 直接购买结算 */
    DIRECT("DIRECT", "直接购买结算"),

    /** 秒杀结算 */
    SECKILL("SECKILL", "秒杀结算");
    /** 数据库存储值 */
    @EnumValue
    private final String code;
    /** 类型描述 */
    private final String desc;
    /**
     * 根据编码查找结算类型。
     *
     * @param code 类型编码，如 "SECKILL"
     * @return 匹配的结算类型
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    public static CheckoutTypeEnum fromCode(String code) {
        return Arrays
                .stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid checkout type: '" + code + "', valid values: "
                                        + Arrays.toString(values())
                        )
                );
    }
}
