package com.cloudmall.mybatisplus.query;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.cloudmall.common.model.page.PageReq;

/**
 * 分页对象工厂。
 * <p>
 * 从纯分页 Req 直接构建 MP Page，Req 中 current/size 已带默认值。
 *
 * @author : Tomatos
 */
public final class Pages {

    /**
     * 从分页请求构建 MP Page。
     *
     * @param req 分页请求
     * @param <T> 实体类型，由赋值目标推导
     * @return MP Page
     */
    public static <T> Page<T> from(PageReq req) {
        return Page.of(req.getCurrent(), req.getSize());
    }

    private Pages() {}
}
