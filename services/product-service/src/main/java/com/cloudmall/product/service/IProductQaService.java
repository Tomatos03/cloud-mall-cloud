package com.cloudmall.product.service;

import java.util.List;

import com.cloudmall.product.api.model.resp.ProductQaResp;

/**
 * 商品问答查询服务。
 *
 * @author : Tomatos
 */
public interface IProductQaService {

    /**
     * 查询商品问答列表。
     *
     * @param productId 商品 SPU ID
     * @param limit 返回条数上限
     * @return 问答列表
     */
    List<ProductQaResp> listByProduct(Long productId, Integer limit);
}
