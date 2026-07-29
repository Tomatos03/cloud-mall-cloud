package com.cloudmall.order.convert;

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
}
