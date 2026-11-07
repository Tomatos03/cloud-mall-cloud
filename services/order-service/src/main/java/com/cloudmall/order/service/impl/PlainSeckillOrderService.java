package com.cloudmall.order.service.impl;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.api.enums.CheckoutTypeEnum;
import com.cloudmall.order.api.model.req.PlainSeckillOrderReq;
import com.cloudmall.order.model.entity.OrderDO;
import com.cloudmall.order.model.entity.OrderItemDO;
import com.cloudmall.order.model.entity.OrderSubDO;
import com.cloudmall.order.enums.ParentOrderStatusEnum;
import com.cloudmall.order.enums.SubOrderStatusEnum;
import com.cloudmall.order.mapper.OrderItemMapper;
import com.cloudmall.order.mapper.OrderMapper;
import com.cloudmall.order.mapper.OrderSubMapper;

/**
 * 普通秒杀同步建单服务
 * <p>
 * 供 seckill-service 普通流程抢购调用：按请求携带的订单快照（秒杀价、商品信息）
 * 单事务幂等落库父订单/子订单/明细，uk_order_no 唯一键兜底并发重复投递。
 * 与结算管道中的 {@code PlaceSeckillOrderStep} 不同：不依赖 cm_seckill_order
 * 快照，订单数据全部来自请求参数。
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlainSeckillOrderService {

    private final OrderMapper orderMapper;
    private final OrderSubMapper orderSubMapper;
    private final OrderItemMapper orderItemMapper;

    /**
     * 普通秒杀同步建单（幂等）。 <p>
     * 单事务插入父订单/子订单/明细；订单号已存在或唯一键冲突时按已有订单幂等返回。
     *
     * @param req 建单请求
     * @return 真实订单号
     */
    @Transactional(rollbackFor = Exception.class)
    public String create(PlainSeckillOrderReq req) {
        String orderNo = req.getOrderNo();
        Asserts.notBlank(orderNo, BizErrorEnum.INVALID_PARAM);

        OrderDO existing = orderMapper.selectByOrderNo(orderNo);
        if (existing != null) {
            log.info("普通秒杀订单已存在，幂等返回 orderNo={}", orderNo);
            return existing.getOrderNo();
        }

        OrderDO order = buildOrder(req);
        if (!tryInsertOrder(order, orderNo)) {
            return Asserts.requireNonNull(orderMapper.selectByOrderNo(orderNo), BizErrorEnum.INVALID_PARAM)
                          .getOrderNo();
        }
        insertSubAndItem(order, req);
        log.info("普通秒杀订单创建成功 orderNo={} userId={}", orderNo, req.getUserId());
        return orderNo;
    }

    /**
     * 插入父订单，唯一键冲突时按已存在订单幂等返回。
     *
     * @param order 父订单
     * @param orderNo 订单号
     * @return true 表示本次插入成功，false 表示并发重复投递
     */
    private boolean tryInsertOrder(OrderDO order, String orderNo) {
        try {
            orderMapper.insert(order);
            return true;
        } catch (DuplicateKeyException e) {
            log.info("普通秒杀订单唯一键冲突，幂等返回 orderNo={}", orderNo);
            return false;
        }
    }

    /**
     * 落库子订单与订单明细。
     *
     * @param order 父订单
     * @param req 建单请求
     */
    private void insertSubAndItem(OrderDO order, PlainSeckillOrderReq req) {
        BigDecimal amount = calcAmount(req);
        OrderSubDO sub = buildSubOrder(order, req.getShopId(), amount);
        orderSubMapper.insert(sub);
        orderItemMapper.insert(buildOrderItem(req, sub.getId(), amount));
    }

    /**
     * 计算应付金额（秒杀价 × 购买数量）。
     *
     * @param req 建单请求
     * @return 应付金额
     */
    private static BigDecimal calcAmount(PlainSeckillOrderReq req) {
        return req.getSeckillPrice().multiply(BigDecimal.valueOf(req.getQuantity()));
    }

    /**
     * 构建待支付父订单（普通秒杀无优惠与运费）。
     *
     * @param req 建单请求
     * @return 父订单实体
     */
    private static OrderDO buildOrder(PlainSeckillOrderReq req) {
        BigDecimal amount = calcAmount(req);
        return OrderDO.builder()
                      .id(IdWorker.getId())
                      .orderNo(req.getOrderNo())
                      .userId(req.getUserId())
                      .orderType(CheckoutTypeEnum.SECKILL)
                      .status(ParentOrderStatusEnum.PENDING_PAYMENT)
                      .totalOriginalAmount(amount)
                      .totalShopDiscountAmount(BigDecimal.ZERO)
                      .totalPlatformDiscountAmount(BigDecimal.ZERO)
                      .productPayableAmount(amount)
                      .freightAmount(BigDecimal.ZERO)
                      .payableAmount(amount)
                      .addressSnapshot(req.getAddressSnapshot())
                      .build();
    }

    /**
     * 构建待发货子订单。
     *
     * @param order 父订单
     * @param shopId 店铺 ID
     * @param amount 应付金额
     * @return 子订单实体
     */
    private static OrderSubDO buildSubOrder(OrderDO order, Long shopId, BigDecimal amount) {
        return OrderSubDO.builder()
                         .id(IdWorker.getId())
                         .subOrderNo(order.getOrderNo() + "-1")
                         .parentOrderId(order.getId())
                         .shopId(shopId)
                         .status(SubOrderStatusEnum.PENDING_SHIPMENT)
                         .originalAmount(amount)
                         .shopDiscountAmount(BigDecimal.ZERO)
                         .platformDiscountAmount(BigDecimal.ZERO)
                         .payableAmount(amount)
                         .merchantSettlementAmount(amount)
                         .build();
    }

    /**
     * 构建订单明细（成交单价固定为秒杀价）。
     *
     * @param req 建单请求
     * @param subOrderId 子订单 ID
     * @param amount 应付金额
     * @return 明细实体
     */
    private static OrderItemDO buildOrderItem(PlainSeckillOrderReq req, Long subOrderId, BigDecimal amount) {
        return OrderItemDO.builder()
                          .subOrderId(subOrderId)
                          .productId(req.getProductId())
                          .skuId(req.getSkuId())
                          .shopId(req.getShopId())
                          .categoryId(req.getCategoryId())
                          .productName(req.getProductName())
                          .productImage(req.getProductImage())
                          .specSnapshot(req.getSpecSnapshot())
                          .unitPrice(req.getSeckillPrice())
                          .quantity(req.getQuantity())
                          .originalAmount(amount)
                          .shopDiscountAmount(BigDecimal.ZERO)
                          .platformDiscountAmount(BigDecimal.ZERO)
                          .payableAmount(amount)
                          .build();
    }
}
