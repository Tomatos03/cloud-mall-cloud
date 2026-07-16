package com.cloudmall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.order.api.request.CreateReq;
import com.cloudmall.order.api.response.ItemResp;
import com.cloudmall.order.api.response.OrderResp;
import com.cloudmall.order.entity.OrderDO;
import com.cloudmall.order.entity.OrderItemDO;
import com.cloudmall.order.mapper.OrderItemMapper;
import com.cloudmall.order.mapper.OrderMapper;
import com.cloudmall.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements IOrderService {

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
        OrderDO order = new OrderDO();
        order.setOrderNo(orderNo);
        order.setUserId(request.getUserId());
        order.setTotalAmount(total);
        order.setStatus("PENDING");
        order.setRemark(request.getRemark());
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);

        // 4. Save order items
        for (CreateReq.OrderItemRequest item : request.getItems()) {
            OrderItemDO oi = new OrderItemDO();
            oi.setOrderId(order.getId());
            oi.setGoodsId(item.getGoodsId());
            oi.setSkuId(item.getSkuId());
            oi.setGoodsName(item.getGoodsName());
            oi.setPrice(item.getPrice());
            oi.setQuantity(item.getQuantity());
            oi.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItemMapper.insert(oi);
        }

        log.info("Order created: {} total={}", orderNo, total);
        return getById(order.getId());
    }

    @Override
    public OrderResp getById(Long id) {
        OrderDO order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException(BizErrorCode.DATA_NOT_FOUND);
        }
        return toResponse(order);
    }

    @Override
    public List<OrderResp> listByUser(Long userId, Integer page, Integer size) {
        List<OrderDO> list = orderMapper.selectPage(
                new Page<>(page != null ? page : 1, size != null ? size : 20),
                new LambdaQueryWrapper<OrderDO>()
                        .eq(OrderDO::getUserId, userId)
                        .orderByDesc(OrderDO::getCreateTime)
        ).getRecords();
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private OrderResp toResponse(OrderDO order) {
        List<OrderItemDO> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItemDO>().eq(OrderItemDO::getOrderId, order.getId())
        );
        OrderResp r = new OrderResp();
        r.setId(order.getId());
        r.setOrderNo(order.getOrderNo());
        r.setUserId(order.getUserId());
        r.setTotalAmount(order.getTotalAmount());
        r.setStatus(order.getStatus());
        r.setRemark(order.getRemark());
        r.setCreateTime(order.getCreateTime());
        r.setItems(items.stream().map(this::toItemResponse).collect(Collectors.toList()));
        return r;
    }

    private ItemResp toItemResponse(OrderItemDO item) {
        ItemResp r = new ItemResp();
        r.setId(item.getId());
        r.setGoodsId(item.getGoodsId());
        r.setSkuId(item.getSkuId());
        r.setGoodsName(item.getGoodsName());
        r.setGoodsImage(item.getGoodsImage());
        r.setPrice(item.getPrice());
        r.setQuantity(item.getQuantity());
        r.setSubtotal(item.getSubtotal());
        return r;
    }
}
