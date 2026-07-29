package com.cloudmall.order.service;

import java.util.List;

import com.cloudmall.order.api.response.ItemResp;

/**
 * 订单明细服务接口
 *
 * @author : Tomatos
 */
public interface IOrderItemService {

    /**
     * 根据订单 ID 查询订单明细
     *
     * @param orderId 订单 ID
     * @return 订单明细列表
     */
    List<ItemResp> listByOrderId(Long orderId);
}
