package com.cloudmall.mybatisplus.query;

import java.util.Collection;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;

import com.cloudmall.mybatisplus.annotation.QueryCondition.Operator;

/**
 * 查询条件构建工具
 * <p>
 * 提供从实体属性到数据库列名的解析、值空性判断、操作符分发等能力，
 * 被 {@code PageReq.buildLambdaWrapper()} 内部调用，也可单独使用。
 *
 * @author : Tomatos
 */
public final class QueryConditionHelper {

    /**
     * 从实体属性名解析数据库列名
     * <p>
     * 通过 MyBatis-Plus {@link TableInfoHelper} 获取实体映射信息，
     * 已支持 {@code @TableField("column_name")} 自定义列名映射。
     *
     * @param entityClass 实体类（需带 {@code @TableName} 注解）
     * @param property    实体属性名（驼峰）
     * @return 数据库列名，未找到时返回 {@code null}
     */
    public static String resolveColumn(Class<?> entityClass, String property) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        if (tableInfo == null) {
            return null;
        }
        return tableInfo.getFieldList()
                        .stream()
                        .filter(f -> f.getProperty().equals(property))
                        .findFirst()
                        .map(TableFieldInfo::getColumn)
                        .orElse(null);
    }

    /**
     * 判断值是否为 null 或空白
     * <p>
     * 字符串判空白（{@link String#isBlank()}），集合判空（{@link Collection#isEmpty()}），
     * 其余类型仅判 null。
     *
     * @param value 待判断值
     * @return true 表示该值应被忽略
     */
    public static boolean isNullOrEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String s) {
            return s.isBlank();
        }
        if (value instanceof Collection<?> c) {
            return c.isEmpty();
        }
        return false;
    }

    /**
     * 按操作符向 Wrapper 追加查询条件
     * <p>
     * 支持 {@link Operator} 中定义的全部操作符。
     * 特殊说明：
     * <ul>
     *   <li>{@code IS_NULL / IS_NOT_NULL} 忽略 value 参数</li>
     *   <li>{@code BETWEEN} 要求 value 为 {@code Object[2]}</li>
     *   <li>{@code IN / NOT_IN} 接受 {@code Collection} 或单个值</li>
     * </ul>
     *
     * @param wrapper  查询条件容器
     * @param column   数据库列名
     * @param operator 操作符
     * @param value    条件值
     */
    public static void applyCondition(
            QueryWrapper<?> wrapper,
            String column,
            Operator operator,
            Object value
    ) {
        switch (operator) {
            case EQ        -> wrapper.eq(column, value);
            case NE        -> wrapper.ne(column, value);
            case LIKE      -> wrapper.like(column, value);
            case LEFT_LIKE -> wrapper.likeLeft(column, value);
            case RIGHT_LIKE -> wrapper.likeRight(column, value);
            case GT        -> wrapper.gt(column, value);
            case GE        -> wrapper.ge(column, value);
            case LT        -> wrapper.lt(column, value);
            case LE        -> wrapper.le(column, value);
            case BETWEEN -> {
                if (value instanceof Object[] a && a.length == 2) {
                    wrapper.between(column, a[0], a[1]);
                }
            }
            case IN -> {
                if (value instanceof Collection<?> c) {
                    wrapper.in(column, c);
                } else {
                    wrapper.in(column, value);
                }
            }
            case NOT_IN -> {
                if (value instanceof Collection<?> c) {
                    wrapper.notIn(column, c);
                } else {
                    wrapper.notIn(column, value);
                }
            }
            case IS_NULL     -> wrapper.isNull(column);
            case IS_NOT_NULL -> wrapper.isNotNull(column);
        }
    }

    private QueryConditionHelper() {
    }
}
