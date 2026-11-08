package com.cloudmall.seckill.api.enums;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 秒杀场次展示状态
 * <p>
 * 用户端秒杀中心按整点展示一天 24 个场次，没有对应活动的场次使用
 * {@link #NOT_OPENED}，其余状态按活动时间窗口实时推导。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum SeckillSessionStatusEnum {
    /** 未开放 — 当前整点没有对应活动 */
    NOT_OPENED("NOT_OPENED", "未开放"),

    /** 即将开始 — 活动开始时间晚于当前时间 */
    UPCOMING("UPCOMING", "即将开始"),

    /** 进行中 — 当前时间处于活动时间窗口内 */
    ONGOING("ONGOING", "进行中"),

    /** 已结束 — 活动结束时间早于当前时间 */
    ENDED("ENDED", "已结束");
    /** 状态编码 */
    private final String code;
    /** 状态描述 */
    private final String desc;
    /**
     * 根据编码查找场次状态。
     *
     * @param code 状态编码
     * @return 匹配的场次状态
     */
    public static SeckillSessionStatusEnum fromCode(String code) {
        return Arrays
                .stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid seckill session status: '" + code + "', valid values: "
                                        + Arrays.toString(values())
                        )
                );
    }
}
