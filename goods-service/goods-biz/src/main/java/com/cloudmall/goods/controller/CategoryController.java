package com.cloudmall.goods.controller;

import com.cloudmall.common.entity.Result;
import com.cloudmall.goods.api.response.CategoryResponse;
import com.cloudmall.goods.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    @GetMapping("/tree")
    public Result<List<CategoryResponse>> tree() {
        return Result.success(categoryService.listTree());
    }

    @GetMapping("/{id}")
    public Result<CategoryResponse> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }
}
