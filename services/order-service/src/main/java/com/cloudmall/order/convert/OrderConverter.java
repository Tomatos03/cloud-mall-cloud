package com.cloudmall.order.convert;

import org.mapstruct.Mapper;

import com.cloudmall.order.api.response.ItemResp;
import com.cloudmall.order.entity.OrderItemDO;

@Mapper(componentModel = "spring")
public interface OrderConverter {

    ItemResp toItemResp(OrderItemDO item);
}
