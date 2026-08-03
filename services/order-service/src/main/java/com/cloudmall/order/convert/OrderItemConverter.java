package com.cloudmall.order.convert;

import java.util.List;

import org.mapstruct.Mapper;

import com.cloudmall.order.api.response.OrderItemResp;
import com.cloudmall.order.entity.OrderItemDO;

/**
 * 订单明细转换器
 * <p>
 * 明细实体与响应字段同名，直接自动映射。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface OrderItemConverter {

    /**
     * 明细实体转响应。
     *
     * @param item 明细实体
     * @return 明细响应
     */
    OrderItemResp toItemResp(OrderItemDO item);

    /**
     * 批量转换明细实体列表为响应列表。
     *
     * @param items 明细实体列表
     * @return 明细响应列表
     */
    List<OrderItemResp> toItemRespList(List<OrderItemDO> items);
}
