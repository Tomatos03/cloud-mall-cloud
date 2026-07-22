package com.cloudmall.goods.service;

import java.util.List;

import com.cloudmall.goods.api.request.SearchReq;
import com.cloudmall.goods.api.response.GoodsResp;

public interface IGoodsService {
    GoodsResp getById(Long id);

    GoodsResp getBySpu(String spu);

    List<GoodsResp> listByCategory(Long categoryId, int page, int size);

    List<GoodsResp> search(SearchReq request);

    Boolean deductStock(Long skuId, Integer quantity);

    Boolean rollbackStock(Long skuId, Integer quantity);
}