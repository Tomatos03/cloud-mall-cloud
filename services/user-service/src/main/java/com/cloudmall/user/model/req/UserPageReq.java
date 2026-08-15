package com.cloudmall.user.model.req;

import lombok.Getter;
import lombok.Setter;

import com.cloudmall.mybatisplus.annotation.Operator;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.model.PageReq;
import com.cloudmall.user.entity.UserDO;

/**
 * 用户分页查询请求
 * <p>
 * 提供分页参数及用户筛选条件。
 *
 * @author : Tomatos
 * @date : 2026/7/29
 */
@Getter
@Setter
public class UserPageReq extends PageReq<UserDO> {

    /** 用户名（模糊查询） */
    @QueryCondition(property = "username", operator = Operator.LIKE)
    private String name;

    /** 是否启用 */
    @QueryCondition
    private Boolean enabled;

    /**
     * 使用指定参数构造对象。
     */
    public UserPageReq() {}

    /**
     * 兼容 URL 参数 ?page=&size=
     *
     * @param page 页码
     */
    public void setPage(int page) {
        setCurrent(page);
    }

    /**
     * 使用指定参数构造对象。
     *
     * @param current 当前页码
     * @param size 页大小
     */
    public UserPageReq(long current, long size) {
        super(current, size);
    }
}
