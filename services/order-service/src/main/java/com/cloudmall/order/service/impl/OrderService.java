package com.cloudmall.order.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.order.api.request.CreateReq;
import com.cloudmall.order.api.response.OrderResp;
import com.cloudmall.order.convert.OrderConverter;
import com.cloudmall.order.convert.OrderItemConverter;
import com.cloudmall.order.entity.OrderDO;
import com.cloudmall.order.entity.OrderItemDO;
import com.cloudmall.order.mapper.OrderItemMapper;
import com.cloudmall.order.mapper.OrderMapper;
import com.cloudmall.order.model.req.OrderPageReq;
import com.cloudmall.order.service.IOrderService;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService implements IOrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderItemConverter orderItemConverter;
    private final OrderConverter orderConverter;

    /**
     * 创建并保存数据。
     *
     * @param request 请求参数
     * @return 处理结果
     */
    @Override
    @Transactional
    public OrderResp createOrder(CreateReq request) {
        String orderNo = UUID.randomUUID().toString().replace("-", "").substring(0, 20);

        BigDecimal total = BigDecimal.ZERO;
        for (CreateReq.OrderItemRequest item : request.getItems()) {
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        OrderDO order = OrderDO.builder()
                .orderNo(orderNo)
                .userId(request.getUserId())
                .totalAmount(total)
                .status("PENDING")
                .remark(request.getRemark())
                .createTime(LocalDateTime.now())
                .build();
        orderMapper.insert(order);

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

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     */
    @Override
    public OrderResp getById(Long id) {
        OrderDO order = orderMapper.selectById(id);
        return toResponse(order);
    }

    /**
     * 分页查询数据。
     *
     * @param req 请求参数
     * @return 分页数据
     */
    @Override
    public IPage<OrderResp> page(OrderPageReq req) {
        IPage<OrderDO> page = orderMapper.selectPage(req, req.buildLambdaWrapper());
        return page.convert(this::toResponse);
    }

    /**
     * 转换对象数据。
     *
     * @param order 订单实体
     * @return 处理结果
     */
    private OrderResp toResponse(OrderDO order) {
        OrderResp resp = orderConverter.toResp(order);
        List<OrderItemDO> items = orderItemMapper.selectList(
                Wrappers.<OrderItemDO>lambdaQuery()
                        .eq(OrderItemDO::getOrderId, order.getId())
        );
        resp.setItems(orderItemConverter.toItemRespList(items));
        return resp;
    }
}
