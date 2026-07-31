package com.cloudmall.order.convert;

import java.util.List;

import org.mapstruct.Mapper;

import com.cloudmall.order.api.response.ItemResp;
import com.cloudmall.order.entity.OrderItemDO;

/**
 * 订单明细转换器
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface OrderItemConverter {

    ItemResp toItemResp(OrderItemDO item);

    /**
     * 批量转换订单明细实体列表为响应列表。
     *
     * @param items 订单明细实体列表
     * @return 订单明细响应列表
     */
    List<ItemResp> toItemRespList(List<OrderItemDO> items);
}
