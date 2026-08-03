package com.cloudmall.product.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.product.api.request.BrandCreateReq;
import com.cloudmall.product.api.request.BrandUpdateReq;
import com.cloudmall.product.api.response.BrandResp;
import com.cloudmall.product.model.req.BrandPageReq;

/**
 * 品牌服务接口。
 *
 * @author : Tomatos
 */
public interface IBrandService {

    /**
     * 根据 ID 获取品牌。
     *
     * @param id 品牌 ID
     * @return 品牌响应
     */
    BrandResp getById(Long id);

    /**
     * 批量获取品牌。
     *
     * @param ids 品牌 ID 列表
     * @return 品牌响应列表
     */
    List<BrandResp> listByIds(List<Long> ids);

    /**
     * 分页查询品牌。
     *
     * @param req 分页请求
     * @return 品牌分页列表
     */
    IPage<BrandResp> page(BrandPageReq req);

    /**
     * 创建品牌。
     *
     * @param req 创建请求
     * @return 新建品牌 ID
     */
    Long create(BrandCreateReq req);

    /**
     * 更新品牌。
     *
     * @param id 品牌 ID
     * @param req 更新请求
     */
    void update(Long id, BrandUpdateReq req);

    /**
     * 批量删除品牌（软删除）。
     *
     * @param ids 品牌 ID 列表
     */
    void delete(List<Long> ids);
}
