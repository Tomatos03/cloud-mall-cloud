package com.cloudmall.order.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.order.api.model.resp.OrderSubResp;
import com.cloudmall.order.model.entity.OrderSubDO;

/**
 * 店铺子订单转换器
 * <p>
 * 枚举状态映射为 code 字符串；明细由 Service 层补充。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface OrderSubConverter {

    /**
     * 子订单实体转响应。
     *
     * @param sub 子订单实体
     * @return 子订单响应
     */
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "status", source = "status.code")
    OrderSubResp toResp(OrderSubDO sub);

    /**
     * 批量转换子订单实体列表为响应列表。
     *
     * @param subs 子订单实体列表
     * @return 子订单响应列表
     */
    List<OrderSubResp> toRespList(List<OrderSubDO> subs);
}
