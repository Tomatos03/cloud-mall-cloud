package com.cloudmall.goods.service;

import java.util.List;

import com.cloudmall.goods.api.request.CategoryCreateReq;
import com.cloudmall.goods.api.response.CategoryNodeResp;
import com.cloudmall.goods.api.response.CategoryResp;

public interface ICategoryService {
    List<CategoryNodeResp> tree();

    CategoryResp getById(Long id);

    Long create(CategoryCreateReq req);
}
