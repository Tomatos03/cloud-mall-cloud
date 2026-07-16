package com.cloudmall.order.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.order.api.request.CreateReq;
import com.cloudmall.order.api.response.ItemResp;
import com.cloudmall.order.api.response.OrderResp;
import com.cloudmall.order.entity.OrderDO;
import com.cloudmall.order.entity.OrderItemDO;
import com.cloudmall.order.mapper.OrderItemMapper;
import com.cloudmall.order.mapper.OrderMapper;
import com.cloudmall.order.service.IOrderService;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService implements IOrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    @Transactional
    public OrderResp createOrder(CreateReq request) {
        // 1. Generate order number
        String orderNo = UUID.randomUUID().toString().replace("-", "").substring(0, 20);

        // 2. Calculate total
        BigDecimal total = BigDecimal.ZERO;
        for (CreateReq.OrderItemRequest item : request.getItems()) {
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // 3. Save order
        OrderDO order = OrderDO.builder()
                .orderNo(orderNo)
                .userId(request.getUserId())
                .totalAmount(total)
                .status("PENDING")
                .remark(request.getRemark())
                .createTime(LocalDateTime.now())
                .build();
        orderMapper.insert(order);

        // 4. Save order items
        for (CreateReq.OrderItemRequest item : request.getItems()) {
            OrderItemDO oi = OrderItemDO.builder()
                    .orderId(order.getId())
                    .goodsId(item.getGoodsId())
                    .skuId(item.getSkuId())
                    .goodsName(item.getGoodsName())
                    .price(item.getPrice())
                    .quantity(item.getQuantity())
                    .subtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();
            orderItemMapper.insert(oi);
        }

        log.info("Order created: {} total={}", orderNo, total);
        return getById(order.getId());
    }

    @Override
    public OrderResp getById(Long id) {
        OrderDO order = orderMapper.selectById(id);
        AssertUtils.notNull(order, BizErrorCode.DATA_NOT_FOUND);
        return toResponse(order);
    }

    @Override
    public List<OrderResp> listByUser(Long userId, Integer page, Integer size) {
        List<OrderDO> list = orderMapper.selectPage(
                new Page<>(page != null ? page : 1, size != null ? size : 20),
                Wrappers.<OrderDO>lambdaQuery()
                        .eq(OrderDO::getUserId, userId)
                        .orderByDesc(OrderDO::getCreateTime)
        ).getRecords();
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private OrderResp toResponse(OrderDO order) {
        List<OrderItemDO> items = orderItemMapper.selectList(
                Wrappers.<OrderItemDO>lambdaQuery().eq(OrderItemDO::getOrderId, order.getId())
        );
        return OrderResp.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .remark(order.getRemark())
                .createTime(order.getCreateTime())
                .items(items.stream().map(this::toItemResponse).collect(Collectors.toList()))
                .build();
    }

    private ItemResp toItemResponse(OrderItemDO item) {
        return ItemResp.builder()
                .id(item.getId())
                .goodsId(item.getGoodsId())
                .skuId(item.getSkuId())
                .goodsName(item.getGoodsName())
                .goodsImage(item.getGoodsImage())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build();
    }
}
