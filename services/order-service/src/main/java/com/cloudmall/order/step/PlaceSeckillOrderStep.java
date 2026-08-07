package com.cloudmall.order.step;

import java.math.BigDecimal;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.api.enums.CheckoutTypeEnum;
import com.cloudmall.order.entity.OrderDO;
import com.cloudmall.order.entity.OrderItemDO;
import com.cloudmall.order.entity.OrderSubDO;
import com.cloudmall.order.enums.ParentOrderStatusEnum;
import com.cloudmall.order.enums.SubOrderStatusEnum;
import com.cloudmall.order.mapper.OrderItemMapper;
import com.cloudmall.order.mapper.OrderMapper;
import com.cloudmall.order.mapper.OrderSubMapper;
import com.cloudmall.order.model.CheckoutContext;
import com.cloudmall.product.api.response.SkuResp;
import com.cloudmall.seckill.api.client.SeckillOrderClient;
import com.cloudmall.seckill.api.response.SeckillOrderResp;

/**
 * 秒杀订单落库步骤
 * <p>
 * 按 orderNo 幂等直接落库父订单、子订单与明细（uk_order_no 唯一键兜底），
 * 单事务执行，成功后将父订单写入 context。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceSeckillOrderStep implements CheckoutStep {

    private final OrderMapper orderMapper;
    private final OrderSubMapper orderSubMapper;
    private final OrderItemMapper orderItemMapper;
    private final SeckillOrderClient seckillOrderClient;

    /**
     * 步骤名称。
     *
     * @return 秒杀订单落库
     */
    @Override
    public CheckoutStepEnum name() {
        return CheckoutStepEnum.PLACE_SECKILL_ORDER;
    }

    /**
     * 执行步骤：幂等落库，成功写入 context.order。
     *
     * @param ctx 订单上下文
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void process(CheckoutContext ctx) {
        String orderNo = ctx.getReq().getOrderNo();
        Asserts.notBlank(orderNo, BizError.INVALID_PARAM);
        OrderDO existing = orderMapper.selectByOrderNo(orderNo);
        if (existing != null) {
            log.info("秒杀订单已存在，幂等返回 orderNo={}", orderNo);
            finish(ctx, existing);
            return;
        }

        SeckillOrderResp seckillOrder = requireSeckillOrder(orderNo);
        BigDecimal amount = calcAmount(seckillOrder);
        OrderDO order = buildOrder(seckillOrder, amount, JSONUtil.toJsonStr(ctx.getAddress()));

        if (!tryInsert(order, orderNo)) {
            finish(ctx, orderMapper.selectByOrderNo(orderNo));
            return;
        }
        insertSubAndItem(order, seckillOrder, ctx, amount);
        finish(ctx, order);
    }

    /**
     * 查询秒杀单快照并校验存在。
     *
     * @param orderNo 订单号
     * @return 秒杀单快照
     */
    private SeckillOrderResp requireSeckillOrder(String orderNo) {
        return Asserts.requireNonNull(seckillOrderClient.getByOrderNo(orderNo), BizError.INVALID_PARAM);
    }

    /**
     * 计算秒杀应付金额（秒杀价 × 购买数量）。
     *
     * @param seckillOrder 秒杀单快照
     * @return 应付金额
     */
    private static BigDecimal calcAmount(SeckillOrderResp seckillOrder) {
        return seckillOrder.getSeckillPrice().multiply(BigDecimal.valueOf(seckillOrder.getQuantity()));
    }

    /**
     * 插入父订单，唯一键冲突时按已存在订单幂等返回。
     *
     * @param order 父订单
     * @param orderNo 订单号
     * @return true 表示本次插入成功，false 表示并发重复投递
     */
    private boolean tryInsert(OrderDO order, String orderNo) {
        try {
            orderMapper.insert(order);
            return true;
        } catch (DuplicateKeyException e) {
            // 并发重复投递：唯一键冲突后回查已有订单
            log.info("秒杀订单唯一键冲突，幂等返回 orderNo={}", orderNo);
            return false;
        }
    }

    /**
     * 落库子订单与订单明细。
     *
     * @param order 父订单
     * @param seckillOrder 秒杀单快照
     * @param ctx 订单上下文
     * @param amount 应付金额
     */
    private void insertSubAndItem(
            OrderDO order,
            SeckillOrderResp seckillOrder,
            CheckoutContext ctx,
            BigDecimal amount
    ) {
        SkuResp sku = ctx.getSkuMap().get(seckillOrder.getSkuId());
        OrderSubDO sub = buildSubOrder(order, sku.getShopId(), amount);
        orderSubMapper.insert(sub);
        orderItemMapper.insert(
                buildOrderItem(seckillOrder, sku, seckillOrder.getQuantity(), sub.getId(), amount)
        );
    }

    /**
     * 写入父订单并记录成功日志。
     *
     * @param ctx 订单上下文
     * @param order 父订单
     */
    private void finish(CheckoutContext ctx, OrderDO order) {
        ctx.setOrder(order);
        log.info("秒杀订单创建成功 orderNo={} userId={}", order.getOrderNo(), order.getUserId());
    }

    /**
     * 构建待支付父订单（秒杀单无优惠与运费）。
     *
     * @param seckillOrder 秒杀单快照
     * @param amount 应付金额
     * @param addressSnapshot 地址快照
     * @return 父订单实体
     */
    private static OrderDO buildOrder(
            SeckillOrderResp seckillOrder,
            BigDecimal amount,
            String addressSnapshot
    ) {
        return OrderDO.builder()
                      .id(IdWorker.getId())
                      .orderNo(seckillOrder.getOrderNo())
                      .userId(seckillOrder.getUserId())
                      .orderType(CheckoutTypeEnum.SECKILL)
                      .status(ParentOrderStatusEnum.PENDING_PAYMENT)
                      .totalOriginalAmount(amount)
                      .totalShopDiscountAmount(BigDecimal.ZERO)
                      .totalPlatformDiscountAmount(BigDecimal.ZERO)
                      .goodsPayableAmount(amount)
                      .freightAmount(BigDecimal.ZERO)
                      .payableAmount(amount)
                      .addressSnapshot(addressSnapshot)
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
     * @param seckillOrder 秒杀单快照
     * @param sku SKU 快照
     * @param quantity 购买数量
     * @param subOrderId 子订单 ID
     * @param amount 应付金额
     * @return 明细实体
     */
    private static OrderItemDO buildOrderItem(
            SeckillOrderResp seckillOrder,
            SkuResp sku,
            Integer quantity,
            Long subOrderId,
            BigDecimal amount
    ) {
        return OrderItemDO.builder()
                          .subOrderId(subOrderId)
                          .productId(seckillOrder.getProductId())
                          .skuId(sku.getSkuId())
                          .shopId(sku.getShopId())
                          .categoryId(sku.getCategoryId())
                          .productName(sku.getProductName())
                          .productImage(sku.getImage())
                          .specSnapshot(sku.getSpecDesc())
                          .unitPrice(seckillOrder.getSeckillPrice())
                          .quantity(quantity)
                          .originalAmount(amount)
                          .shopDiscountAmount(BigDecimal.ZERO)
                          .platformDiscountAmount(BigDecimal.ZERO)
                          .payableAmount(amount)
                          .build();
    }
}
