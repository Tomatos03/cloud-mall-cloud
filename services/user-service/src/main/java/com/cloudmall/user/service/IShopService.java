package com.cloudmall.user.service;

import java.util.List;

import com.cloudmall.user.api.response.ShopResp;
import com.cloudmall.user.model.req.ShopGetReq;

public interface IShopService {
    ShopResp get(ShopGetReq req);

    /**
     * 批量查询店铺信息。
     *
     * @param shopIds 店铺 ID 列表
     * @return 店铺信息列表
     */
    List<ShopResp> listByIds(List<Long> shopIds);
}
