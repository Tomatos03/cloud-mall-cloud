package com.cloudmall.product.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.product.api.request.UnitCreateReq;
import com.cloudmall.product.api.request.UnitUpdateReq;
import com.cloudmall.product.api.response.UnitResp;
import com.cloudmall.product.model.req.UnitPageReq;

/**
 * 商品单位服务接口。
 *
 * @author : Tomatos
 */
public interface IUnitService {

    /**
     * 根据 ID 获取单位。
     *
     * @param id 单位 ID
     * @return 单位响应
     */
    UnitResp getById(Long id);

    /**
     * 分页查询单位列表。
     *
     * @param req 分页请求
     * @return 单位分页列表
     */
    IPage<UnitResp> page(UnitPageReq req);

    /**
     * 创建单位。
     *
     * @param req 创建请求
     * @return 新建单位 ID
     */
    Long create(UnitCreateReq req);

    /**
     * 更新单位。
     *
     * @param id 单位 ID
     * @param req 更新请求
     */
    void update(Long id, UnitUpdateReq req);

    /**
     * 批量删除单位（软删除）。
     *
     * @param ids 单位 ID 列表
     */
    void delete(List<Long> ids);
}
