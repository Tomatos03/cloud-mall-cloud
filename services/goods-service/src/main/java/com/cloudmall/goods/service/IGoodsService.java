package com.cloudmall.goods.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudmall.goods.api.response.GoodsResp;
import com.cloudmall.goods.model.req.GoodsPageReq;

public interface IGoodsService {
    GoodsResp getById(Long id);

    GoodsResp getBySpu(String spu);

    IPage<GoodsResp> page(GoodsPageReq req);

    Boolean deductStock(Long skuId, Integer quantity);

    Boolean rollbackStock(Long skuId, Integer quantity);
}
