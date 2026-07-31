package com.cloudmall.product.controller;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.product.api.request.UnitCreateReq;
import com.cloudmall.product.api.request.UnitUpdateReq;
import com.cloudmall.product.api.response.UnitResp;
import com.cloudmall.product.model.req.UnitPageReq;
import com.cloudmall.product.service.IUnitService;

/**
 * 商品单位 Controller。
 * <p>
 * 提供单位的增删改查功能。
 *
 * @author : Tomatos
 */
@RestController
@RequestMapping("/units")
@RequiredArgsConstructor
public class UnitController {

    private final IUnitService unitService;

    /**
     * 根据 ID 获取单位。
     *
     * @param id 单位 ID
     * @return 单位信息
     */
    @GetMapping("/{id}")
    public Result<UnitResp> getById(@PathVariable Long id) {
        return Result.success(unitService.getById(id));
    }

    /**
     * 分页查询单位列表。
     *
     * @param req 分页 + 筛选条件
     * @return 单位分页列表
     */
    @GetMapping
    public Result<IPage<UnitResp>> page(UnitPageReq req) {
        return Result.success(unitService.page(req));
    }

    /**
     * 创建单位。
     *
     * @param req 创建请求
     * @return 新建单位 ID
     */
    @PostMapping
    public Result<Long> create(@RequestBody UnitCreateReq req) {
        return Result.success(unitService.create(req));
    }

    /**
     * 更新单位。
     *
     * @param id  单位 ID
     * @param req 更新请求
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UnitUpdateReq req) {
        unitService.update(id, req);
        return Result.success();
    }

    /**
     * 批量删除单位（软删除）。
     *
     * @param ids 单位 ID 列表
     * @return 操作结果
     */
    @DeleteMapping
    public Result<Void> delete(@RequestBody List<Long> ids) {
        unitService.delete(ids);
        return Result.success();
    }
}
