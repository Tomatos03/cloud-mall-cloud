package com.cloudmall.user.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.model.entity.BrowseHistoryDO;

/**
 * 用户浏览记录 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface BrowseHistoryMapper extends BaseMapper<BrowseHistoryDO> {

    /**
     * 查询用户浏览记录，按浏览时间倒序。
     *
     * @param userId 用户 ID
     * @return 浏览记录列表
     */
    default List<BrowseHistoryDO> listByUserId(Long userId) {
        return selectList(
                Wrappers.<BrowseHistoryDO>lambdaQuery()
                        .eq(BrowseHistoryDO::getUserId, userId)
                        .orderByDesc(BrowseHistoryDO::getBrowseTime)
        );
    }

    /**
     * 查询用户对指定商品的浏览记录。
     *
     * @param userId 用户 ID
     * @param productId 商品 ID
     * @return 浏览记录，不存在时返回 null
     */
    default BrowseHistoryDO selectByUserAndProduct(Long userId, Long productId) {
        return selectOne(
                Wrappers.<BrowseHistoryDO>lambdaQuery()
                        .eq(BrowseHistoryDO::getUserId, userId)
                        .eq(BrowseHistoryDO::getProductId, productId)
        );
    }

    /**
     * 删除用户对指定商品的浏览记录。
     *
     * @param userId 用户 ID
     * @param productId 商品 ID
     * @return 影响行数
     */
    default int deleteByUserAndProduct(Long userId, Long productId) {
        return delete(
                Wrappers.<BrowseHistoryDO>lambdaQuery()
                        .eq(BrowseHistoryDO::getUserId, userId)
                        .eq(BrowseHistoryDO::getProductId, productId)
        );
    }

    /**
     * 清空用户全部浏览记录。
     *
     * @param userId 用户 ID
     * @return 影响行数
     */
    default int deleteByUserId(Long userId) {
        return delete(Wrappers.<BrowseHistoryDO>lambdaQuery().eq(BrowseHistoryDO::getUserId, userId));
    }
}
