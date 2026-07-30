package com.cloudmall.product.service;

import java.util.List;

import com.cloudmall.product.api.request.CategoryCreateReq;
import com.cloudmall.product.api.response.CategoryNodeResp;
import com.cloudmall.product.api.response.CategoryResp;

public interface ICategoryService {
    List<CategoryNodeResp> tree();

    CategoryResp getById(Long id);

    Long create(CategoryCreateReq req);
}
