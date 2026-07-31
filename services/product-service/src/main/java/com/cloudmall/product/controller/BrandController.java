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
import com.cloudmall.product.api.request.BrandCreateReq;
import com.cloudmall.product.api.request.BrandUpdateReq;
import com.cloudmall.product.api.response.BrandResp;
import com.cloudmall.product.model.req.BrandPageReq;
import com.cloudmall.product.service.IBrandService;

/**
 * 品牌 Controller。
 * <p>
 * 提供品牌的增删改查功能。
 *
 * @author : Tomatos
 */
@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BrandController {

    private final IBrandService brandService;

    /**
     * 根据 ID 获取品牌。
     *
     * @param id 品牌 ID
     * @return 品牌信息
     */
    @GetMapping("/{id}")
    public Result<BrandResp> getById(@PathVariable Long id) {
        return Result.success(brandService.getById(id));
    }

    /**
     * 分页查询品牌列表。
     *
     * @param req 分页 + 筛选条件
     * @return 品牌分页列表
     */
    @GetMapping
    public Result<IPage<BrandResp>> page(BrandPageReq req) {
        return Result.success(brandService.page(req));
    }

    /**
     * 创建品牌。
     *
     * @param req 创建请求
     * @return 新建品牌 ID
     */
    @PostMapping
    public Result<Long> create(@RequestBody BrandCreateReq req) {
        return Result.success(brandService.create(req));
    }

    /**
     * 更新品牌。
     *
     * @param id  品牌 ID
     * @param req 更新请求
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody BrandUpdateReq req) {
        brandService.update(id, req);
        return Result.success();
    }

    /**
     * 批量删除品牌（软删除）。
     *
     * @param ids 品牌 ID 列表
     * @return 操作结果
     */
    @DeleteMapping
    public Result<Void> delete(@RequestBody List<Long> ids) {
        brandService.delete(ids);
        return Result.success();
    }
}
