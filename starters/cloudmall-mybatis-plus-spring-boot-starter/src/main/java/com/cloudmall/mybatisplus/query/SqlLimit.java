package com.cloudmall.mybatisplus.query;

import cn.hutool.core.lang.Assert;

/**
 * LIMIT 子句工厂
 * <p>
 * 统一构造 {@code LIMIT n} 片段，供 {@code Wrapper#last(String)} 使用，
 * 避免在 Mapper 中手写 SQL 片段。
 *
 * <pre>{@code
 * .last(SqlLimit.of(10))
 * }</pre>
 *
 * @author : Tomatos
 */
public final class SqlLimit {

    /**
     * 构造 LIMIT 子句
     *
     * @param limit 行数上限，必须为正数
     * @return "LIMIT N"
     * @throws IllegalArgumentException limit 为 0 或负数时抛出
     */
    public static String of(int limit) {
        Assert.isTrue(limit > 0, "limit must be positive, but got {}", limit);
        return "LIMIT " + limit;
    }

    private SqlLimit() {}
}
