package com.cloudmall.goods.controller;

import java.util.List;

import com.cloudmall.goods.api.request.CategoryCreateReq;
import com.cloudmall.goods.api.response.CategoryNodeResp;
import com.cloudmall.goods.api.response.CategoryResp;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.goods.service.ICategoryService;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    @GetMapping("/tree")
    public Result<List<CategoryNodeResp>> tree() {
        return Result.success(categoryService.tree());
    }

    @GetMapping("/{id}")
    public Result<CategoryResp> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @PostMapping
    public Result<Long> create(@RequestBody CategoryCreateReq req) {
        return Result.success(categoryService.create(req));
    }
}
