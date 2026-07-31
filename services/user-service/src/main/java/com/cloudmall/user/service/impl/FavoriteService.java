package com.cloudmall.user.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.user.entity.FavoriteDO;
import com.cloudmall.user.mapper.FavoriteMapper;
import com.cloudmall.user.service.IFavoriteService;

@Service
@RequiredArgsConstructor
public class FavoriteService implements IFavoriteService {

    private final FavoriteMapper favoriteMapper;

    /**
     * 转换对象数据。
     *
     * @param userId 用户标识
     * @param productId 商品标识
     * @return 是否满足条件
     */
    @Override
    public boolean toggle(Long userId, Long productId) {
        FavoriteDO existing = favoriteMapper.selectOne(
                Wrappers.<FavoriteDO>lambdaQuery()
                        .eq(FavoriteDO::getUserId, userId)
                        .eq(FavoriteDO::getProductId, productId)
        );
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            return false;
        } else {
            FavoriteDO favorite = FavoriteDO
                    .builder()
                    .userId(userId)
                    .productId(productId)
                    .createTime(LocalDateTime.now())
                    .build();
            favoriteMapper.insert(favorite);
            return true;
        }
    }

    /**
     * 查询数据列表。
     *
     * @param userId 用户标识
     * @return 数据列表
     */
    @Override
    public List<Long> listProductIds(Long userId) {
        List<FavoriteDO> list = favoriteMapper.selectList(
                Wrappers.<FavoriteDO>lambdaQuery()
                        .eq(FavoriteDO::getUserId, userId)
                        .orderByDesc(FavoriteDO::getCreateTime)
        );
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
        return favoriteMapper.selectCount(
                Wrappers.<FavoriteDO>lambdaQuery()
                        .eq(FavoriteDO::getUserId, userId)
                        .eq(FavoriteDO::getProductId, productId)
        ) > 0;
    }
}
