package com.cloudmall.order.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mapping;

import com.cloudmall.order.api.response.OrderResp;
import com.cloudmall.order.entity.OrderDO;

@Mapper(componentModel = "spring")
public interface OrderConverter {

    /**
     * 转换对象数据。
     *
     * @param order 订单实体
     * @return 处理结果
     */
    @Mapping(target = "items", ignore = true)
    OrderResp toResp(OrderDO order);
}
