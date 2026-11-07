package com.cloudmall.product.service;

import java.util.List;
import java.util.Map;

import com.cloudmall.product.api.model.resp.SpecGroupResp;

public interface IProductSpecService {

    /**
     * 获取商品的所有规格组及其规格值
     *
     * @param productId 商品 ID
     * @return 规格组响应对象列表
     */
    List<SpecGroupResp> getSpecGroups(Long productId);

    /**
     * 批量获取 SKU 对应的规格值 ID 映射
     *
     * @param skuIds SKU ID 列表
     * @return SKU ID -> 规格值 ID 列表的映射
     */
    Map<Long, List<Long>> getSkuSpecMap(List<Long> skuIds);

    /**
     * 查询商品中同时包含所有指定规格值的 SKU 映射
     *
     * @param productId 商品 ID
     * @param specValueIds 规格值 ID 列表
     * @return SKU ID -> 规格值 ID 列表的映射
     */
    Map<Long, List<Long>> getSpecValueSkuMap(Long productId, List<Long> specValueIds);
}
