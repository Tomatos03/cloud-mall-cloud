package com.cloudmall.mybatisplus.model;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.cloudmall.mybatisplus.query.ConditionBuilder;

/**
 * 分页请求基类
 * <p>
 * 继承 MyBatis-Plus {@link Page}，提供默认分页参数。
 * 条件构建由 {@link ConditionBuilder} 的默认实现自动处理。
 *
 * <pre>{@code
 * &#064;Getter &#064;Setter
 * public class UserPageReq extends PageReq&lt;UserDO&gt; {
 *
 *     &#064;QueryCondition(property = "username", operator = OperatorEnum.LIKE)
 *     private String name;
 *
 *     &#064;QueryCondition(operator = OperatorEnum.EQ)
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
public class PageReq<T> extends Page<T> implements ConditionBuilder<T> {

    /**
     * 使用指定参数构造对象。
     */
    public PageReq() {
        this(1, 10);
    }

    /**
     * 构建分页请求
     *
     * @param current 页码，从 1 开始
     * @param size 每页条数
     */
    public PageReq(long current, long size) {
        super(current, size);
    }

    /**
     * 兼容前端 page 参数。
     *
     * @param page 页码，从 1 开始
     */
    public void setPage(long page) {
        setCurrent(page);
    }

    /**
     * 兼容前端 pageSize 参数。
     *
     * @param pageSize 每页条数
     */
    public void setPageSize(long pageSize) {
        setSize(pageSize);
    }
}
