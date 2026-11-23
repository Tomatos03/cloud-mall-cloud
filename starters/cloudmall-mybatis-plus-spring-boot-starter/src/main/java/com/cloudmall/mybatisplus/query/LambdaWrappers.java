package com.cloudmall.mybatisplus.query;

import java.lang.reflect.Field;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.cloudmall.mybatisplus.annotation.OperatorEnum;
import com.cloudmall.mybatisplus.annotation.QueryCondition;

/**
 * 查询条件包装器工厂。
 * <p>
 * 扫描请求对象上标注 {@link QueryCondition} 的字段，按注解声明的属性映射与
 * 操作符生成 WHERE 条件。请求对象上的 {@code @QueryCondition} 注解直接决定
 * 生成的 SQL，增删注解等同于修改查询逻辑。实体类型由调用方显式传入，
 * 因此请求对象本身不需要引用 DO。
 *
 * @author : Tomatos
 */
public final class LambdaWrappers {

    /**
     * 从带注解的请求对象构建查询条件。
     *
     * @param source 条件对象，其 {@code @QueryCondition} 字段会被扫
     *               描
     * @param entityClass 实体类型，由 service 层提供
     * @param <T> 实体类型
     * @return 查询条件包装器
     */
    public static <T> LambdaQueryWrapper<T> of(Object source, Class<T> entityClass) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        for (Field field : ReflectUtil.getFields(source.getClass())) {
            QueryCondition ann = field.getAnnotation(QueryCondition.class);
            if (ann == null) {
                continue;
            }
            String property = StrUtil.isBlank(ann.property()) ? field.getName() : ann.property();
            String column = QueryConditionHelper.resolveColumn(entityClass, property);
            if (column == null) {
                continue;
            }
            Object value = ReflectUtil.getFieldValue(source, field);
            boolean isSelfSufficient = ann.operator() == OperatorEnum.IS_NULL
                                               || ann.operator() == OperatorEnum.IS_NOT_NULL;
            if (!isSelfSufficient && ann.ignoreNull() && QueryConditionHelper.isNullOrEmpty(value)) {
                continue;
            }
            QueryConditionHelper.applyCondition(wrapper, column, ann.operator(), value);
        }
        return wrapper.lambda();
    }

    private LambdaWrappers() {}
}
