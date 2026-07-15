package com.cloudmall.goods.service;

import com.cloudmall.goods.api.response.CategoryResp;

import java.util.List;

public interface ICategoryService {
    List<CategoryResp> listTree();

    CategoryResp getById(Long id);
}
