package com.cloudmall.seckill.enums;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 秒杀 Lua 抢购结果枚举
 * <p>
 * 对应 resources/lua/seckill_purchase.lua 的返回码：1=成功，-2=已抢购，-3=库存不足。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum SeckillPurchaseResultEnum {
    /** 抢购成功 */
    SUCCESS(1L, "成功"),

    /** 已抢购过（一人一单） */
    REPEAT(-2L, "已抢购"),

    /** 库存不足 */
    NO_STOCK(-3L, "库存不足");
    /** Lua 返回码 */
    private final Long code;
    /** 结果描述 */
    private final String desc;
    /**
     * 根据 Lua 返回码查找抢购结果。
     *
     * @param code 返回码
     * @return 匹配的抢购结果
     * @throws IllegalArgumentException 返回码为 null 或未命中时抛出
     */
    public static SeckillPurchaseResultEnum fromCode(Long code) {
        return Arrays
                .stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid seckill purchase result code: '" + code + "', valid values: "
                                        + Arrays.toString(values())
                        )
                );
    }
}
