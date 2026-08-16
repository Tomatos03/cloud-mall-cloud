package com.cloudmall.product.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.product.api.request.CategoryCreateReq;
import com.cloudmall.product.api.response.CategoryNodeResp;
import com.cloudmall.product.api.response.CategoryResp;
import com.cloudmall.product.service.ICategoryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final ICategoryService categoryService;

    /**
     * 查询启用中的分类平铺列表（含层级关系，按排序值升序）。
     *
     * @return 处理结果
     */
    @GetMapping
    public Result<List<CategoryResp>> list() {
        return Result.success(categoryService.listEnabled());
    }

    /**
     * 查询分类树。
     *
     * @return 处理结果
     */
    @GetMapping("/tree")
    public Result<List<CategoryNodeResp>> tree() {
        return Result.success(categoryService.tree());
    }

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     */
    @GetMapping("/{id}")
    public Result<CategoryResp> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    /**
     * 批量查询分类（内部计算/筛选用）。
     *
     * @param ids 分类 ID 列表
     * @return 分类响应列表
     */
    @PostMapping("/batch")
    public Result<List<CategoryResp>> batch(@RequestBody List<Long> ids) {
        return Result.success(categoryService.getByIds(ids));
    }

    /**
     * 创建并保存数据。
     *
     * @param req 请求参数
     * @return 处理结果
     */
    @PostMapping
    public Result<Void> create(@RequestBody CategoryCreateReq req) {
        categoryService.create(req);
        return Result.success();
    }
}
