package com.cloudmall.order.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单类型枚举
 * <p>
 * 对应 cm_order.order_type 列。秒杀单跳过购物车、优惠券与普通库存预占，
 * 由 seckill-service 预生成订单号后经内部接口建单。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum OrderTypeEnum {
    /** 普通订单 */
    NORMAL("NORMAL", "普通订单"),

    /** 秒杀订单 */
    SECKILL("SECKILL", "秒杀订单");
    /** 数据库存储值 */
    @EnumValue
    private final String code;
    /** 类型描述 */
    private final String desc;
    /**
     * 根据编码查找订单类型。
     *
     * @param code 类型编码，如 "SECKILL"
     * @return 匹配的订单类型
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    public static OrderTypeEnum fromCode(String code) {
        return Arrays
                .stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid order type: '" + code + "', valid values: "
                                        + Arrays.toString(values())
                        )
                );
    }
}
