package com.cloudmall.mybatisplus.query;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.cloudmall.mybatisplus.annotation.Operator;
import com.cloudmall.mybatisplus.annotation.QueryCondition;

/**
 * 查询条件构建器接口。
 * <p>
 * 实现类通过在字段上标注 {@link QueryCondition @QueryCondition}
 * 声明条件映射，{@link #buildLambdaWrapper()} 自动生成查询条件，
 * 消除 Service 层手写 Wrapper 代码。
 *
 * <p>接口已提供完整的默认实现——子类只需声明 {@code @QueryCondition} 字段，
 * 通常无需覆盖 {@code buildLambdaWrapper()}。
 *
 * <pre>{@code
 * &#064;Getter &#064;Setter
 * public class GoodsListReq implements ConditionBuilder&lt;GoodsDO&gt; {
 *
 *     &#064;QueryCondition(operator = Operator.EQ)
 *     private Long categoryId;
 *
 *     &#064;QueryCondition(operator = Operator.LIKE)
 *     private String keyword;
 * }
 * }</pre>
 *
 * @param <T> 实体类型
 * @author : Tomatos
 */
public interface ConditionBuilder<T> {

    /**
     * 构建查询条件。
     * <p>
     * 遍历实现类中所有带 {@code @QueryCondition} 的字段，
     * 按注解声明的操作符和属性映射生成 {@link LambdaQueryWrapper}。
     * 值为 {@code null} 或空白时自动跳过（{@code IS_NULL / IS_NOT_NULL} 除外）。
     *
     * @return LambdaQueryWrapper，包含所有非空注解条件
     */
    default LambdaQueryWrapper<T> buildLambdaWrapper() {
        Class<T> entityClass = resolveEntityClass();
        if (entityClass == null) {
            return new QueryWrapper<T>().lambda();
        }

        QueryWrapper<T> wrapper = new QueryWrapper<>();
        for (Field field : ReflectUtil.getFields(getClass())) {
            QueryCondition ann = field.getAnnotation(QueryCondition.class);
            if (ann == null) {
                continue;
            }

            String property = StrUtil.isBlank(ann.property()) ? field.getName() : ann.property();
            String column = QueryConditionHelper.resolveColumn(entityClass, property);
            if (column == null) {
                continue;
            }

            Object value = ReflectUtil.getFieldValue(this, field);
            boolean isSelfSufficient = ann.operator() == Operator.IS_NULL
                    || ann.operator() == Operator.IS_NOT_NULL;
            if (!isSelfSufficient && ann.ignoreNull()
                    && QueryConditionHelper.isNullOrEmpty(value)) {
                continue;
            }

            QueryConditionHelper.applyCondition(wrapper, column, ann.operator(), value);
        }
        return wrapper.lambda();
    }

    /**
     * 从子类泛型参数推断实体类型。
     * <p>
     * 通过反射获取父类的类型参数 {@code T} 的实际类型。
     * 例如 {@code GoodsListReq extends ConditionReq<GoodsDO>} 返回 {@code GoodsDO.class}。
     *
     * @return 处理结果
     */
    @SuppressWarnings("unchecked")
    private Class<T> resolveEntityClass() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType parameterizedType) {
            Type[] typeArgs = parameterizedType.getActualTypeArguments();
            if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?> typeArg) {
                return (Class<T>) typeArg;
            }
        }
        return null;
    }
}
