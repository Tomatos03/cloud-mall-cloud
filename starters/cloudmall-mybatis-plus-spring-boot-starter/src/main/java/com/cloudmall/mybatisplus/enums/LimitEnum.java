package com.cloudmall.mybatisplus.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用 LIMIT 子句枚举
 *
 * @author : Tomatos
 * @date : 2026/7/22
 */
@Getter
@AllArgsConstructor
public enum LimitEnum {
    ONE("LIMIT 1"),
    TEN("LIMIT 10"),
    TWENTY("LIMIT 20"),
    THIRTY("LIMIT 30"),
    FIFTY("LIMIT 50"),
    HUNDRED("LIMIT 100"),
    TWO_HUNDRED("LIMIT 200"),
    THOUSAND("LIMIT 1000");
    private final String sql;
    /**
     * 动态创建 LIMIT 子句
     *
     * @param limit 行数上限，必须为正数
     * @return "LIMIT N"
     * @throws IllegalArgumentException limit 为 0 或负数时抛出
     */
    public static String of(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive, but got " + limit);
        }
        return String.format("LIMIT %s", limit);
    }
}
