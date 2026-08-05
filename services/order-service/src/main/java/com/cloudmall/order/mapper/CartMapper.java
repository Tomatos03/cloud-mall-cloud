package com.cloudmall.order.mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.order.entity.CartDO;

/**
 * 购物车 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface CartMapper extends BaseMapper<CartDO> {

    /**
     * 按用户与 SKU 查询购物车记录。
     *
     * @param userId 用户 ID
     * @param skuId SKU ID
     * @return 购物车记录，不存在时返回 null
     */
    default CartDO selectByUserIdAndSkuId(Long userId, Long skuId) {
        return selectOne(
                Wrappers.<CartDO>lambdaQuery()
                        .eq(CartDO::getUserId, userId)
                        .eq(CartDO::getSkuId, skuId)
        );
    }

    /**
     * 查询用户购物车记录（按加入时间排序）。
     *
     * @param userId 用户 ID
     * @return 购物车记录列表
     */
    default List<CartDO> listByUserId(Long userId) {
        return selectList(
                Wrappers.<CartDO>lambdaQuery()
                        .eq(CartDO::getUserId, userId)
                        .orderByAsc(CartDO::getCreateTime)
        );
    }

    /**
     * 查询用户已选中的购物车项。
     *
     * @param userId 用户 ID
     * @return 已选中购物车项列表
     */
    default List<CartDO> listSelectedByUserId(Long userId) {
        return selectList(
                Wrappers.<CartDO>lambdaQuery()
                        .eq(CartDO::getUserId, userId)
                        .eq(CartDO::getSelected, Boolean.TRUE)
        );
    }

    /**
     * 统计用户购物车商品种数。
     *
     * @param userId 用户 ID
     * @return 购物车记录数
     */
    default Long countByUserId(Long userId) {
        return selectCount(Wrappers.<CartDO>lambdaQuery().eq(CartDO::getUserId, userId));
    }

    /**
     * 删除用户购物车中的指定 SKU 记录。
     *
     * @param userId 用户 ID
     * @param skuIds SKU ID 集合
     * @return 影响行数
     */
    default int deleteByUserIdAndSkuIds(Long userId, Collection<Long> skuIds) {
        return delete(
                Wrappers.<CartDO>lambdaQuery()
                        .eq(CartDO::getUserId, userId)
                        .in(CartDO::getSkuId, skuIds)
        );
    }

    /**
     * 清空用户购物车。
     *
     * @param userId 用户 ID
     * @return 影响行数
     */
    default int deleteByUserId(Long userId) {
        return delete(Wrappers.<CartDO>lambdaQuery().eq(CartDO::getUserId, userId));
    }

    /**
     * 更新购物车数量（仅当现有数量大于 0，防止误更新不存在的记录）。
     *
     * @param userId 用户 ID
     * @param skuId SKU ID
     * @param quantity 目标数量
     * @return 影响行数，0 表示记录不存在
     */
    default int updateQuantityIfPositive(Long userId, Long skuId, Integer quantity) {
        return update(
                Wrappers.<CartDO>lambdaUpdate()
                        .set(CartDO::getQuantity, quantity)
                        .gt(CartDO::getQuantity, 0)
                        .eq(CartDO::getUserId, userId)
                        .eq(CartDO::getSkuId, skuId)
        );
    }

    /**
     * 更新单个 SKU 的选中状态。
     *
     * @param userId 用户 ID
     * @param skuId SKU ID
     * @param selected 是否选中
     * @return 影响行数，0 表示记录不存在
     */
    default int updateSelectedByUserIdAndSkuId(Long userId, Long skuId, Boolean selected) {
        return update(
                Wrappers.<CartDO>lambdaUpdate()
                        .set(CartDO::getSelected, selected)
                        .eq(CartDO::getUserId, userId)
                        .eq(CartDO::getSkuId, skuId)
        );
    }

    /**
     * 更新用户购物车全选状态。
     *
     * @param userId 用户 ID
     * @param selected 是否选中
     * @return 影响行数
     */
    default int updateSelectedByUserId(Long userId, Boolean selected) {
        return update(
                Wrappers.<CartDO>lambdaUpdate()
                        .set(CartDO::getSelected, selected)
                        .eq(CartDO::getUserId, userId)
        );
    }

    /**
     * 更新店铺下所有购物车项的选中状态。
     *
     * @param userId 用户 ID
     * @param shopId 店铺 ID
     * @param selected 是否选中
     * @return 影响行数
     */
    default int updateSelectedByUserIdAndShopId(Long userId, Long shopId, Boolean selected) {
        return update(
                Wrappers.<CartDO>lambdaUpdate()
                        .set(CartDO::getSelected, selected)
                        .eq(CartDO::getUserId, userId)
                        .eq(CartDO::getShopId, shopId)
        );
    }

    /**
     * 已存在记录合并：累加数量并刷新价格（带数量并发匹配条件）。
     *
     * @param id 购物车记录 ID
     * @param quantity 合并后数量
     * @param price 最新单价
     * @param quantityCondition 当前行数量需满足的匹配值
     * @return 影响行数，0 表示记录已变更
     */
    default int mergeQuantityAndPrice(Long id, Integer quantity, BigDecimal price, Integer quantityCondition) {
        return update(
                Wrappers.<CartDO>lambdaUpdate()
                        .set(CartDO::getQuantity, quantity)
                        .set(CartDO::getPrice, price)
                        .eq(CartDO::getId, id)
                        .eq(CartDO::getQuantity, quantityCondition)
        );
    }
}
