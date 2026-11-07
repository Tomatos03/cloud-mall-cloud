package com.cloudmall.product.service;

import java.util.List;

import com.cloudmall.product.api.model.resp.ImageResp;

public interface IProductImageService {

    /**
     * 根据商品 ID 获取图片列表，按主图优先、排序号升序排列
     *
     * @param productId 商品 ID
     * @return 图片响应对象列表
     */
    List<ImageResp> listByProductId(Long productId);

}
