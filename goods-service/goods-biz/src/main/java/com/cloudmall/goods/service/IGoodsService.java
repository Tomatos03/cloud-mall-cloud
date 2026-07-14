package com.cloudmall.goods.service;

import com.cloudmall.goods.api.request.GoodsSearchRequest;
import com.cloudmall.goods.api.response.GoodsResponse;

import java.util.List;

public interface IGoodsService {
    GoodsResponse getById(Long id);

    List<GoodsResponse> listByCategory(Long categoryId, int page, int size);

    List<GoodsResponse> search(GoodsSearchRequest request);

    Boolean deductStock(Long skuId, Integer quantity);

    Boolean rollbackStock(Long skuId, Integer quantity);
}
