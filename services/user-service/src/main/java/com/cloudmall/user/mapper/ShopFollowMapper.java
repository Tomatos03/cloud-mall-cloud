package com.cloudmall.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.model.entity.ShopFollowDO;

/**
 * 用户关注店铺关系 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface ShopFollowMapper extends BaseMapper<ShopFollowDO> {

    /**
     * 查询用户与店铺的关注关系。
     *
     * @param userId 用户 ID
     * @param shopId 店铺 ID
     * @return 关注记录，未关注时返回 null
     */
    default ShopFollowDO selectByUserAndShop(Long userId, Long shopId) {
        return selectOne(
                Wrappers.<ShopFollowDO>lambdaQuery()
                        .eq(ShopFollowDO::getUserId, userId)
                        .eq(ShopFollowDO::getShopId, shopId)
        );
    }

    /**
     * 删除用户与店铺的关注关系。
     *
     * @param userId 用户 ID
     * @param shopId 店铺 ID
     * @return 影响行数
     */
    default int deleteByUserAndShop(Long userId, Long shopId) {
        return delete(
                Wrappers.<ShopFollowDO>lambdaQuery()
                        .eq(ShopFollowDO::getUserId, userId)
                        .eq(ShopFollowDO::getShopId, shopId)
        );
    }
}
