package com.cloudmall.mybatisplus.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明 Req 字段到实体属性的查询条件映射
 * <p>
 * 标注在 {@code PageReq} 子类的字段上，配合 {@code buildLambdaWrapper()} 自动生成查询条件。
 * 字段名与实体属性名一致时无需指定 {@code property}，不一致时需显式映射。
 *
 * <pre>{@code
 * // 例 1：字段名 = 实体属性名，等值查询
 * @QueryCondition(operator = EQ)
 * private Long categoryId;
 *
 * // 例 2：字段名 ≠ 实体属性名，模糊查询
 * @QueryCondition(property = "username", operator = LIKE)
 * private String name;
 *
 * // 例 3：范围查询（拆为两个字段）
 * @QueryCondition(property = "createTime", operator = GE)
 * private LocalDateTime startTime;
 * @QueryCondition(property = "createTime", operator = LE)
 * private LocalDateTime endTime;
 * }</pre>
 *
 * @author : Tomatos
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QueryCondition {

    /**
     * 实体属性名。
     * <p>
     * 默认取被注解字段名，当 Req 字段名与实体属性名不一致时需显式指定。
     * 例如 Req 中 {@code name} 对应实体 {@code UserDO.username}，则 {@code property = "username"}。
     */
    String property() default "";

    /**
     * 查询操作符。
     */
    Operator operator() default Operator.EQ;

    /**
     * 值为 {@code null} 或空白字符串/空集合时是否忽略该条件。
     * <p>
     * 为 {@code true} 时，值为空则不追加到 WHERE 条件中，避免生成无效 SQL。
     */
    boolean ignoreNull() default true;

    /**
     * 查询操作符枚举
     */
    enum Operator {

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
}
