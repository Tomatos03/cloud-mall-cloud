package com.cloudmall.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.goods.entity.FavoriteDO;
import com.cloudmall.goods.mapper.FavoriteMapper;
import com.cloudmall.goods.service.IFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements IFavoriteService {

    private final FavoriteMapper favoriteMapper;

    @Override
    public boolean toggle(Long userId, Long goodsId) {
        FavoriteDO existing = favoriteMapper.selectOne(
                new LambdaQueryWrapper<FavoriteDO>()
                        .eq(FavoriteDO::getUserId, userId)
                        .eq(FavoriteDO::getGoodsId, goodsId)
        );
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            return false;
        } else {
            FavoriteDO f = new FavoriteDO();
            f.setUserId(userId);
            f.setGoodsId(goodsId);
            f.setCreateTime(LocalDateTime.now());
            favoriteMapper.insert(f);
            return true;
        }
    }

    @Override
    public List<Long> listGoodsIds(Long userId) {
        List<FavoriteDO> list = favoriteMapper.selectList(
                new LambdaQueryWrapper<FavoriteDO>()
                        .eq(FavoriteDO::getUserId, userId)
                        .orderByDesc(FavoriteDO::getCreateTime)
        );
        return list.stream().map(FavoriteDO::getGoodsId).collect(Collectors.toList());
    }

    @Override
    public boolean isFavorited(Long userId, Long goodsId) {
        return favoriteMapper.selectCount(
                new LambdaQueryWrapper<FavoriteDO>()
                        .eq(FavoriteDO::getUserId, userId)
                        .eq(FavoriteDO::getGoodsId, goodsId)
        ) > 0;
    }
}
