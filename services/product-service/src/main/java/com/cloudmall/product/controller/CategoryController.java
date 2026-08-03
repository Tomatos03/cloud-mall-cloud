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
    public Result<CategoryResp> getById(
            @PathVariable
            Long id
    ) {
        return Result.success(categoryService.getById(id));
    }

    /**
     * 创建并保存数据。
     *
     * @param req 请求参数
     * @return 处理结果
     */
    @PostMapping
    public Result<Void> create(
            @RequestBody
            CategoryCreateReq req
    ) {
        categoryService.create(req);
        return Result.success();
    }
}
