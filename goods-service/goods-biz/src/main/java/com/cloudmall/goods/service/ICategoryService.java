package com.cloudmall.goods.service;

import com.cloudmall.goods.api.response.CategoryResponse;

import java.util.List;

public interface ICategoryService {
    List<CategoryResponse> listTree();

    CategoryResponse getById(Long id);
}
