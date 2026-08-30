package com.cloudmall.user.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.cloudmall.user.entity.FavoriteDO;
import com.cloudmall.user.mapper.FavoriteMapper;
import com.cloudmall.user.service.IFavoriteService;

@Service
@RequiredArgsConstructor
public class FavoriteService implements IFavoriteService {

    private final FavoriteMapper favoriteMapper;

    /**
     * 收藏商品，已收藏则直接返回。
     *
     * @param userId 用户标识
     * @param productId 商品标识
     */
    @Override
    public void favorite(Long userId, Long productId) {
        try {
            FavoriteDO favorite = FavoriteDO.builder()
                                            .userId(userId)
                                            .productId(productId)
                                            .createTime(LocalDateTime.now())
                                            .build();
            favoriteMapper.insert(favorite);
        } catch (DuplicateKeyException ignored) {
        }
    }

    /**
     * 取消收藏商品，未收藏则直接返回。
     *
     * @param userId 用户标识
     * @param productId 商品标识
     */
    @Override
    public void unfavorite(Long userId, Long productId) {
        favoriteMapper.cancelFavorite(userId, productId);
    }

    /**
     * 查询数据列表。
     *
     * @param userId 用户标识
     * @return 数据列表
     */
    @Override
    public List<Long> listProductIds(Long userId) {
        List<FavoriteDO> list = favoriteMapper.listByUserId(userId);
        return list.stream()
                   .map(FavoriteDO::getProductId)
                   .toList();
    }

    /**
     * 判断是否满足条件。
     *
     * @param userId 用户标识
     * @param productId 商品标识
     * @return 是否满足条件
     */
    @Override
    public boolean isFavorited(Long userId, Long productId) {
        return favoriteMapper.countByUserAndProduct(userId, productId) > 0;
    }
}
