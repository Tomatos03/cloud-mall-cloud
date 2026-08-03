package com.cloudmall.order.service;

import java.util.List;

import com.cloudmall.order.api.request.CartOperatorReq;
import com.cloudmall.order.api.request.CartSelectReq;
import com.cloudmall.order.api.response.CartDetailResp;
import com.cloudmall.order.entity.CartDO;

/**
 * 购物车服务接口
 *
 * @author : Tomatos
 */
public interface ICartService {

    /**
     * 添加商品到购物车
     *
     * @param userId 用户 ID
     * @param req 添加购物车请求
     * @return 购物车记录 ID
     */
    Long addToCart(Long userId, CartOperatorReq req);

    /**
     * 更新购物车商品数量
     *
     * @param userId 用户 ID
     * @param req 更新数量请求（skuId + quantity）
     */
    void updateQuantity(Long userId, CartOperatorReq req);

    /**
     * 移除购物车中的商品
     *
     * @param userId 用户 ID
     * @param skuId SKU ID
     */
    void removeItems(Long userId, List<Long> skuId);

    /**
     * 选中/取消选中指定商品
     *
     * @param userId 用户 ID
     * @param req 选中请求（skuId + selected）
     */
    void selectItems(Long userId, CartSelectReq req);

    /**
     * 全选/全不选
     *
     * @param userId 用户 ID
     * @param selected true=全选，false=全不选
     */
    void selectAll(Long userId, Boolean selected);

    /**
     * 选中/取消选中指定店铺的所有商品
     *
     * @param userId 用户 ID
     * @param shopId 店铺 ID
     * @param selected true=选中，false=取消选中
     */
    void selectShop(Long userId, Long shopId, Boolean selected);

    /**
     * 获取用户购物车内容 <p>
     * 返回按店铺分组的商品列表及全车汇总统计。
     *
     * @param userId 用户 ID
     * @return 购物车摘要（含分组明细）
     */
    CartDetailResp getCart(Long userId);

    /**
     * 清空用户购物车
     *
     * @param userId 用户 ID
     */
    void clear(Long userId);

    /**
     * 查询当前用户已选中的购物车项。
     *
     * @param userId 用户 ID
     * @return 已选中购物车项列表
     */
    List<CartDO> listSelectedItems(Long userId);
}
