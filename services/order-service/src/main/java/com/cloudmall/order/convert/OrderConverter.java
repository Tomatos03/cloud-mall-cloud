package com.cloudmall.order.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.order.api.model.resp.OrderResp;
import com.cloudmall.order.model.entity.OrderDO;

/**
 * 父订单转换器
 * <p>
 * 枚举状态映射为 code 字符串；子订单与券快照由 Service 层补充。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface OrderConverter {

    /**
     * 父订单实体转响应。
     *
     * @param order 父订单实体
     * @return 父订单响应
     */
    @Mapping(target = "subs", ignore = true)
    @Mapping(target = "coupons", ignore = true)
    @Mapping(target = "status", source = "status.code")
    OrderResp toResp(OrderDO order);
}
