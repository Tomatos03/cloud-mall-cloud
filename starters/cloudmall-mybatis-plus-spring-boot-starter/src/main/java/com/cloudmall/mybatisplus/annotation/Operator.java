package com.cloudmall.mybatisplus.annotation;

/**
 * 查询操作符枚举。
 * <p>
 * 配合 {@link QueryCondition @QueryCondition} 注解声明字段的查询方式。
 * 原为 {@code QueryCondition} 的内部枚举，提取为顶层类以简化使用书写。
 *
 * @author : Tomatos
 */
public enum Operator {
    /** 等值查询 {@code column = ?} */
    EQ,
    /** 不等查询 {@code column <> ?} */
    NE,

    /** 模糊匹配 {@code column LIKE '%?%'} */
    LIKE,
    /** 左模糊 {@code column LIKE '%?'} */
    LEFT_LIKE,
    /** 右模糊 {@code column LIKE '?%'} */
    RIGHT_LIKE,

    /** 大于 {@code column > ?} */
    GT,
    /** 大于等于 {@code column >= ?} */
    GE,
    /** 小于 {@code column < ?} */
    LT,
    /** 小于等于 {@code column <= ?} */
    LE,

    /** 范围查询 {@code column BETWEEN ? AND ?}，值类型为 {@code Object[2]} */
    BETWEEN,

    /** IN 查询 {@code column IN (?, ?, ...)}，值类型为 {@code Collection} */
    IN,
    /** NOT IN 查询 {@code column NOT IN (?, ?, ...)}，值类型为 {@code Collection} */
    NOT_IN,

    /** IS NULL {@code column IS NULL} */
    IS_NULL,
    /** IS NOT NULL {@code column IS NOT NULL} */
    IS_NOT_NULL,
}
