package com.cloudmall.user.mapper;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.model.entity.ShopStatDO;

/**
 * 店铺统计 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface ShopStatMapper extends BaseMapper<ShopStatDO> {

    /**
     * 关注数加一。
     *
     * @param shopId 店铺 ID
     * @return 影响行数
     */
    default int incrementFollowCount(Long shopId) {
        return update(
                Wrappers.<ShopStatDO>lambdaUpdate()
                        .setSql("follow_count = follow_count + 1")
                        .set(ShopStatDO::getUpdateTime, LocalDateTime.now())
                        .eq(ShopStatDO::getShopId, shopId)
        );
    }

    /**
     * 关注数减一（不会减为负数）。
     *
     * @param shopId 店铺 ID
     * @return 影响行数
     */
    default int decrementFollowCount(Long shopId) {
        return update(
                Wrappers.<ShopStatDO>lambdaUpdate()
                        .setSql("follow_count = GREATEST(follow_count - 1, 0)")
                        .set(ShopStatDO::getUpdateTime, LocalDateTime.now())
                        .eq(ShopStatDO::getShopId, shopId)
        );
    }
}
