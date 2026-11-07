package com.cloudmall.user.service;

import java.util.List;
import java.util.Optional;

import com.cloudmall.user.api.model.resp.ShopInfoResp;
import com.cloudmall.user.api.model.resp.ShopResp;
import com.cloudmall.user.model.req.ShopReq;

public interface IShopService {

    /**
     * 获取相关数据。
     *
     * @param req 请求参数
     * @return 处理结果
     */
    ShopResp get(ShopReq req);

    /**
     * 查询用户对应的店铺 ID。
     *
     * @param userId 用户 ID
     * @return 店铺 ID，未开店时为空
     */
    Optional<Long> findShopIdByUserId(Long userId);

    /**
     * 查询店铺信息（含评分、商品数、好评率、关注数）。
     *
     * @param shopId 店铺 ID
     * @return 店铺信息
     */
    ShopInfoResp getShopInfo(Long shopId);

    /**
     * 批量查询店铺信息。
     *
     * @param shopIds 店铺 ID 列表
     * @return 店铺信息列表
     */
    List<ShopResp> listByIds(List<Long> shopIds);
}
