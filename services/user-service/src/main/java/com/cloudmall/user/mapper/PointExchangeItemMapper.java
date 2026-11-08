package com.cloudmall.user.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.model.entity.PointExchangeItemDO;

/**
 * 积分兑换商品 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface PointExchangeItemMapper extends BaseMapper<PointExchangeItemDO> {

    /**
     * 查询启用的兑换商品。
     *
     * @return 兑换商品列表
     */
    default List<PointExchangeItemDO> listEnabled() {
        return selectList(
                Wrappers.<PointExchangeItemDO>lambdaQuery()
                        .eq(PointExchangeItemDO::getEnabled, true)
                        .orderByAsc(PointExchangeItemDO::getSort)
        );
    }

    /**
     * 条件扣减库存。
     *
     * @param itemId 商品 ID
     * @return 影响行数，0 表示库存不足
     */
    default int deductStock(Long itemId) {
        return update(
                Wrappers.<PointExchangeItemDO>lambdaUpdate()
                        .setSql("stock = stock - 1")
                        .eq(PointExchangeItemDO::getId, itemId)
                        .gt(PointExchangeItemDO::getStock, 0)
        );
    }
}
