package com.cloudmall.user.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.model.entity.FavoriteDO;

@Mapper
public interface FavoriteMapper extends BaseMapper<FavoriteDO> {

    /**
     * 查询用户收藏的指定商品（未收藏返回 null）。
     *
     * @param userId 用户标识
     * @param productId 商品标识
     * @return 收藏记录，未收藏时为 null
     */
    default FavoriteDO selectByUserAndProduct(Long userId, Long productId) {
        return selectOne(
                Wrappers.<FavoriteDO>lambdaQuery()
                        .eq(FavoriteDO::getUserId, userId)
                        .eq(FavoriteDO::getProductId, productId)
        );
    }

    default void cancelFavorite(Long userId, Long productId) {
        delete(
                Wrappers.<FavoriteDO>lambdaQuery()
                        .eq(FavoriteDO::getUserId, userId)
                        .eq(FavoriteDO::getProductId, productId)
        );
    }

    /**
     * 查询用户全部收藏，按收藏时间倒序。
     *
     * @param userId 用户标识
     * @return 收藏记录列表
     */
    default List<FavoriteDO> listByUserId(Long userId) {
        return selectList(
                Wrappers.<FavoriteDO>lambdaQuery()
                        .eq(FavoriteDO::getUserId, userId)
                        .orderByDesc(FavoriteDO::getCreateTime)
        );
    }

    /**
     * 统计用户收藏的指定商品数量（0 或 1）。
     *
     * @param userId 用户标识
     * @param productId 商品标识
     * @return 收藏记录数量
     */
    default Long countByUserAndProduct(Long userId, Long productId) {
        return selectCount(
                Wrappers.<FavoriteDO>lambdaQuery()
                        .eq(FavoriteDO::getUserId, userId)
                        .eq(FavoriteDO::getProductId, productId)
        );
    }
}
