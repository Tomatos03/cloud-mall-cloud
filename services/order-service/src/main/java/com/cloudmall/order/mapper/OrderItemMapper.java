package com.cloudmall.order.mapper;

import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
    @Select(
            """
            SELECT *
            FROM cm_order_item
            WHERE sub_order_id = #{subOrderId} AND deleted = 0
            ORDER BY id
            """
    )
    List<OrderItemDO> selectBySubOrderId(
            @Param("subOrderId")
            Long subOrderId
    );

    /**
     * 批量查询多个子订单下的全部明细。
     *
     * @param subOrderIds 子订单 ID 集合
     * @return 明细列表
     */
    @Select(
            """
            <script>
            SELECT *
            FROM cm_order_item
            WHERE deleted = 0 AND sub_order_id IN
            <foreach collection="subOrderIds" item="subOrderId" open="(" separator="," close=")">
                #{subOrderId}
            </foreach>
            ORDER BY id
            </script>
            """
    )
    List<OrderItemDO> selectBySubOrderIds(
            @Param("subOrderIds")
            Collection<Long> subOrderIds
    );
}
