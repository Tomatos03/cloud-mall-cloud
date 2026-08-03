package com.cloudmall.product.service;

import java.util.List;

import com.cloudmall.product.api.request.CategoryCreateReq;
import com.cloudmall.product.api.response.CategoryNodeResp;
import com.cloudmall.product.api.response.CategoryResp;

public interface ICategoryService {

    /**
     * 查询分类树。
     *
     * @return 数据列表
     */
    List<CategoryNodeResp> tree();

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     */
    CategoryResp getById(Long id);

    /**
     * 批量查询分类（忽略已删除）。
     *
     * @param ids 分类 ID 列表
     * @return 分类响应列表
     */
    List<CategoryResp> getByIds(List<Long> ids);

    /**
     * 创建并保存数据。
     *
     * @param req 请求参数
     */
    void create(CategoryCreateReq req);
}
