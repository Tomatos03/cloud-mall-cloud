package com.cloudmall.goods.service;

import java.util.List;

import com.cloudmall.goods.api.response.CategoryResp;

public interface ICategoryService {
    List<CategoryResp> listTree();

    CategoryResp getById(Long id);
}