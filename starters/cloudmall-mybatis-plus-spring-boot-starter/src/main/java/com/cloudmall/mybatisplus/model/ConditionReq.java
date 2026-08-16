package com.cloudmall.mybatisplus.model;

import com.cloudmall.mybatisplus.query.ConditionBuilder;

/**
 * 通用条件查询请求基类。
 * <p>
 * 空的标记类，继承 {@link ConditionBuilder} 默认实现。
 * 子类只需声明 {@code @QueryCondition} 字段，无需任何覆盖。
 *
 * <pre>{@code
 * &#064;Getter &#064;Setter
 * public class ProductConditionReq extends ConditionReq&lt;ProductDO&gt; {
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
public class ConditionReq<T> implements ConditionBuilder<T> {
}
