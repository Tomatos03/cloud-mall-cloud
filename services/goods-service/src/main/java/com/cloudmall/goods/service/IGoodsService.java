package com.cloudmall.goods.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudmall.goods.api.response.GoodsResp;
import com.cloudmall.goods.model.req.GoodsGetReq;
import com.cloudmall.goods.model.req.GoodsPageReq;

public interface IGoodsService {
    GoodsResp get(GoodsGetReq req);

    IPage<GoodsResp> page(GoodsPageReq req);

    Boolean deductStock(Long skuId, Integer quantity);

    Boolean rollbackStock(Long skuId, Integer quantity);

    /**
     * 更新商品SKU库存
     * <p>
     * quantity >= 0 时回滚库存（增加），quantity < 0 时扣减库存（减少）。
     *
     * @param skuId    SKU ID
     * @param quantity 库存变化量（正数回滚，负数扣减）
     * @return 操作结果
     */
    Boolean updateStock(Long skuId, Integer quantity);
}
