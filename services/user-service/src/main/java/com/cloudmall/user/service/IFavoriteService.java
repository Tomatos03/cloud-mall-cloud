package com.cloudmall.user.service;

import java.util.List;

/**
 * 收藏服务。
 */
public interface IFavoriteService {

    /**
     * 收藏商品，已收藏则直接返回。
     *
     * @param userId 用户 ID
     * @param productId 商品 ID
     */
    void favorite(Long userId, Long productId);

    /**
     * 取消收藏商品，未收藏则直接返回。
     *
     * @param userId 用户 ID
     * @param productId 商品 ID
     */
    void unfavorite(Long userId, Long productId);

    /**
     * 查询用户收藏的商品 ID 列表。
     *
     * @param userId 用户 ID
     * @return 商品 ID 列表，按收藏时间倒序
     */
    List<Long> listProductIds(Long userId);

    /**
     * 判断用户是否已收藏指定商品。
     *
     * @param userId 用户 ID
     * @param productId 商品 ID
     * @return 已收藏返回 true，未收藏返回 false
     */
    boolean isFavorited(Long userId, Long productId);
}
