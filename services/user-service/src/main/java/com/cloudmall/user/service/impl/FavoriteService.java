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
            FavoriteDO favorite = FavoriteDO.builder()
                    .userId(userId)
                    .productId(productId)
                    .createTime(LocalDateTime.now())
                    .build();
            favoriteMapper.insert(favorite);
            return true;
        }
    }

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

    @Override
    public boolean isFavorited(Long userId, Long productId) {
        return favoriteMapper.selectCount(
                Wrappers.<FavoriteDO>lambdaQuery()
                        .eq(FavoriteDO::getUserId, userId)
                        .eq(FavoriteDO::getProductId, productId)
        ) > 0;
    }
}
