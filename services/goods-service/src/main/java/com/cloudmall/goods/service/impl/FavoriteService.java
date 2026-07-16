package com.cloudmall.goods.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.goods.entity.FavoriteDO;
import com.cloudmall.goods.mapper.FavoriteMapper;
import com.cloudmall.goods.service.IFavoriteService;

@Service
@RequiredArgsConstructor
public class FavoriteService implements IFavoriteService {

    private final FavoriteMapper favoriteMapper;

    @Override
    public boolean toggle(Long userId, Long goodsId) {
        FavoriteDO existing = favoriteMapper.selectOne(
                Wrappers.<FavoriteDO>lambdaQuery()
                        .eq(FavoriteDO::getUserId, userId)
                        .eq(FavoriteDO::getGoodsId, goodsId)
        );
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            return false;
        } else {
            FavoriteDO f = FavoriteDO.builder()
                    .userId(userId)
                    .goodsId(goodsId)
                    .createTime(LocalDateTime.now())
                    .build();
            favoriteMapper.insert(f);
            return true;
        }
    }

    @Override
    public List<Long> listGoodsIds(Long userId) {
        List<FavoriteDO> list = favoriteMapper.selectList(
                Wrappers.<FavoriteDO>lambdaQuery()
                        .eq(FavoriteDO::getUserId, userId)
                        .orderByDesc(FavoriteDO::getCreateTime)
        );
        return list.stream().map(FavoriteDO::getGoodsId).collect(Collectors.toList());
    }

    @Override
    public boolean isFavorited(Long userId, Long goodsId) {
        return favoriteMapper.selectCount(
                Wrappers.<FavoriteDO>lambdaQuery()
                        .eq(FavoriteDO::getUserId, userId)
                        .eq(FavoriteDO::getGoodsId, goodsId)
        ) > 0;
    }
}
