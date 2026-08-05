package com.cloudmall.order.mapper;

import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.order.entity.OrderItemDO;

/**
 * 订单明细 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItemDO> {

    /**
     * 查询子订单下的全部明细。
     *
     * @param subOrderId 子订单 ID
     * @return 明细列表
     */
    default List<OrderItemDO> selectBySubOrderId(Long subOrderId) {
        return selectList(
                Wrappers.<OrderItemDO>lambdaQuery()
                        .eq(OrderItemDO::getSubOrderId, subOrderId)
                        .orderByAsc(OrderItemDO::getId)
        );
    }

    /**
     * 批量查询多个子订单下的全部明细。
     *
     * @param subOrderIds 子订单 ID 集合
     * @return 明细列表
     */
    default List<OrderItemDO> selectBySubOrderIds(Collection<Long> subOrderIds) {
        return selectList(
                Wrappers.<OrderItemDO>lambdaQuery()
                        .in(OrderItemDO::getSubOrderId, subOrderIds)
                        .orderByAsc(OrderItemDO::getId)
        );
    }
}
