package com.cloudmall.order.model;

import java.util.Map;

import com.cloudmall.order.entity.OrderDO;
import com.cloudmall.order.entity.OrderSubDO;

/**
 * 下单创建结果：内存中构建完成的父订单与店铺子订单。
 * <p>
 * 仅承载纯内存构建产物，持久化由 {@code OrderService} 统一落库。
 *
 * @param order 父订单（id 已预生成）
 * @param subsByShop shopId → 子订单
 * @author : Tomatos
 */
public record OrderCreationResult(OrderDO order, Map<Long, OrderSubDO> subsByShop) {
}
