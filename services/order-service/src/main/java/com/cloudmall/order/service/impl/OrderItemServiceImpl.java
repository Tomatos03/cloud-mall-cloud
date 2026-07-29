package com.cloudmall.order.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.order.api.response.ItemResp;
import com.cloudmall.order.convert.OrderItemConverter;
import com.cloudmall.order.entity.OrderItemDO;
import com.cloudmall.order.mapper.OrderItemMapper;
import com.cloudmall.order.service.IOrderItemService;

/**
 * 订单明细服务实现
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements IOrderItemService {

    private final OrderItemMapper orderItemMapper;
    private final OrderItemConverter orderItemConverter;

    @Override
    public List<ItemResp> listByOrderId(Long orderId) {
        return orderItemMapper.selectList(
                Wrappers.<OrderItemDO>lambdaQuery().eq(OrderItemDO::getOrderId, orderId)
        ).stream()
         .map(orderItemConverter::toItemResp)
         .toList();
    }
}
