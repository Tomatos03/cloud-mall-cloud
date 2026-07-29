package com.cloudmall.mybatisplus.model;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.query.QueryConditionHelper;

/**
 * 分页请求基类
 * <p>
 * 继承 MyBatis-Plus {@link Page}，提供默认分页参数和注解驱动的自动条件构建。
 * 业务模块需继承此类并声明泛型参数 {@code <T>}（实体类），
 * 在子类字段上标注 {@link QueryCondition} 声明查询映射。
 *
 * <pre>{@code
 * &#064;Getter &#064;Setter
 * public class UserPageReq extends PageReq&lt;UserDO&gt; {
 *
 *     &#064;QueryCondition(property = "username", operator = LIKE)
 *     private String name;
 *
 *     &#064;QueryCondition(operator = EQ)
 *     private Boolean enabled;
 * }
 * }</pre>
 *
 * Service 层调用：
 * <pre>{@code
 * LambdaQueryWrapper<UserDO> wrapper = req.buildLambdaWrapper();
 * userMapper.selectPage(req, wrapper);
 * }</pre>
 *
 * @param <T> 实体类型
 * @author : Tomatos
 * @date : 2026/7/29
 */
public class PageReq<T> extends Page<T> {

    public PageReq() {
        this(1, 10);
    }

    /**
     * 构建分页请求
     *
     * @param current 页码，从 1 开始
     * @param size    每页条数
     */
    public PageReq(long current, long size) {
        super(current, size);
    }

    /**
     * 自动从 {@link QueryCondition @QueryCondition} 注解构建查询条件
     * <p>
     * 遍历当前对象中所有带 {@code @QueryCondition} 的字段，依次执行：
     * <ol>
     *   <li>解析实体属性名对应的数据库列名</li>
     *   <li>获取字段值，按 {@code ignoreNull} 策略判空</li>
     *   <li>按注解声明的操作符追加条件</li>
     * </ol>
     *
     * <p>内部使用 {@link QueryWrapper}（字符串列名）构建条件，
     * 因为 {@link LambdaQueryWrapper} 的类型参数 {@code R = SFunction}，不支持字符串方法。
     * 最终通过 {@code wrapper.lambda()} 转换为 {@code LambdaQueryWrapper<T>} 返回，
     * 便于调用方混用 Lambda 条件（如 {@code wrapper.and(w -> w.eq(UserDO::getUsername, ...))}）。
     *
     * @return LambdaQueryWrapper<T> 包含所有非空注解条件
     */
    public LambdaQueryWrapper<T> buildLambdaWrapper() {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        Class<?> entityClass = resolveEntityClass();
        if (entityClass == null) {
            return wrapper.lambda();
        }

        for (Field field : ReflectUtil.getFields(getClass())) {
            QueryCondition ann = field.getAnnotation(QueryCondition.class);
            if (ann == null) {
                continue;
            }

            String property = ann.property().isEmpty() ? field.getName() : ann.property();
            String column = QueryConditionHelper.resolveColumn(entityClass, property);
            if (column == null) {
                continue;
            }

            Object value = ReflectUtil.getFieldValue(this, field);
            // IS_NULL / IS_NOT_NULL 是自足的操作符，不需要值，仅凭注解即表达语义
            boolean isSelfSufficient = ann.operator() == QueryCondition.Operator.IS_NULL
                    || ann.operator() == QueryCondition.Operator.IS_NOT_NULL;
            if (!isSelfSufficient && ann.ignoreNull() && QueryConditionHelper.isNullOrEmpty(value)) {
                continue;
            }

            QueryConditionHelper.applyCondition(wrapper, column, ann.operator(), value);
        }
        return wrapper.lambda();
    }

    /**
     * 从泛型参数推断实体类
     * <p>
     * 通过反射获取 {@code PageReq<T>} 的类型参数 {@code T} 的实际类型。
     * 例如 {@code UserPageReq extends PageReq<UserDO>} 返回 {@code UserDO.class}。
     *
     * @return 实体类，无法推断时返回 {@code null}
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
