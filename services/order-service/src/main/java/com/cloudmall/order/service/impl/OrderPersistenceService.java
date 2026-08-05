package com.cloudmall.order.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.coupon.api.response.CouponItemResult;
import com.cloudmall.coupon.api.response.CouponOrderCalcResp;
import com.cloudmall.coupon.api.response.CouponSubOrderResult;
import com.cloudmall.coupon.api.response.CouponUsageResult;
import com.cloudmall.coupon.enums.CouponOwnerEnum;
import com.cloudmall.order.convert.OrderCouponConverter;
import com.cloudmall.order.entity.OrderCouponDO;
import com.cloudmall.order.entity.OrderDO;
import com.cloudmall.order.entity.OrderItemDO;
import com.cloudmall.order.entity.OrderSubDO;
import com.cloudmall.order.enums.ParentOrderStatusEnum;
import com.cloudmall.order.enums.SubOrderStatusEnum;
import com.cloudmall.order.mapper.OrderCouponMapper;
import com.cloudmall.order.mapper.OrderItemMapper;
import com.cloudmall.order.mapper.OrderMapper;
import com.cloudmall.order.mapper.OrderSubMapper;
import com.cloudmall.order.model.OrderContext;
import com.cloudmall.order.model.OrderCreationResult;
import com.cloudmall.order.model.OrderDraft;
import com.cloudmall.product.api.response.SkuResp;

/**
 * 订单持久化服务
 * <p>
 * 承载下单 Saga 中需要独立事务提交的写库步骤：
 * CREATING 骨架、金额/明细/优惠落库、失败标记取消。
 * 骨架必须先于远程调用提交，宕机时才能被超时恢复任务发现并补偿。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class OrderPersistenceService {

    private final OrderMapper orderMapper;
    private final OrderSubMapper orderSubMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderCouponMapper orderCouponMapper;
    private final OrderCouponConverter orderCouponConverter;

    /**
     * 下单骨架：CREATING 父订单与每个店铺一条子订单。 <p>
     * 订单号在内部生成，父订单 ID 预生成雪花值以便内存中完成子订单组装， 再统一批量落库，保证同一事务
     * 内骨架完整提交。
     *
     * @param context 订单上下文
     * @return 骨架
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderDraft createDraft(OrderContext context) {
        OrderCreationResult creationResult = buildOrderCreationResult(context);
        saveOrderWithSubs(creationResult);
        return new OrderDraft(creationResult.order(), creationResult.subsByShop());
    }

    /**
     * 纯内存构建父订单与子订单，无任何副作用。
     *
     * @param context 订单上下文
     * @return 构建结果
     */
    private static OrderCreationResult buildOrderCreationResult(OrderContext context) {
        String orderNo = genOrderNo();
        OrderDO order = buildCreatingOrder(context, orderNo);
        order.setId(IdWorker.getId());
        Map<Long, OrderSubDO> subsByShop = new LinkedHashMap<>();
        for (int i = 0; i < context.getShopIds().size(); i++) {
            Long shopId = context.getShopIds().get(i);
            subsByShop.put(shopId, buildPendingSubOrder(orderNo, i, order, shopId));
        }
        return new OrderCreationResult(order, subsByShop);
    }

    /**
     * 批量持久化父订单与子订单。
     *
     * @param creationResult 构建结果
     */
    private void saveOrderWithSubs(OrderCreationResult creationResult) {
        orderMapper.insert(creationResult.order());
        orderSubMapper.insert(creationResult.subsByShop().values());
    }

    private static String genOrderNo() {
        return StrUtil.sub(IdUtil.fastSimpleUUID(), 0, 20);
    }

    private static OrderSubDO buildPendingSubOrder(String orderNo, int i, OrderDO order, Long shopId) {
        return OrderSubDO.builder()
                         .subOrderNo(orderNo + "-" + (i + 1))
                         .parentOrderId(order.getId())
                         .shopId(shopId)
                         .status(SubOrderStatusEnum.PENDING_SHIPMENT)
                         .originalAmount(BigDecimal.ZERO)
                         .shopDiscountAmount(BigDecimal.ZERO)
                         .platformDiscountAmount(BigDecimal.ZERO)
                         .payableAmount(BigDecimal.ZERO)
                         .merchantSettlementAmount(BigDecimal.ZERO)
                         .build();
    }

    private static OrderDO buildCreatingOrder(OrderContext context, String orderNo) {
        return OrderDO.builder()
                      .orderNo(orderNo)
                      .userId(context.getUserId())
                      .status(ParentOrderStatusEnum.CREATING)
                      .totalOriginalAmount(BigDecimal.ZERO)
                      .totalShopDiscountAmount(BigDecimal.ZERO)
                      .totalPlatformDiscountAmount(BigDecimal.ZERO)
                      .goodsPayableAmount(BigDecimal.ZERO)
                      .freightAmount(BigDecimal.ZERO)
                      .payableAmount(BigDecimal.ZERO)
                      .addressSnapshot(JSONUtil.toJsonStr(context.getAddress()))
                      .remark(context.getReq().getRemark())
                      .build();
    }

    /**
     * 落库订单金额、明细优惠分摊与券快照，并将父订单迁移为待支付。
     *
     * @param orderDraft 骨架
     * @param context 订单上下文
     * @param calc 优惠计算/锁定结果
     */
    @Transactional(rollbackFor = Exception.class)
    public void finalizeOrder(OrderDraft orderDraft, OrderContext context, CouponOrderCalcResp calc) {
        updateParentAmounts(orderDraft.order().getId(), calc);
        processSubOrders(orderDraft, context, calc);
        insertOrderCoupons(orderDraft, calc.getUsages());
        transitionToPendingPayment(orderDraft.order().getId());
    }

    /**
     * 按优惠结果处理所有子订单：更新金额并插入商品明细。
     *
     * @param orderDraft 骨架
     * @param context 订单上下文
     * @param calc 优惠计算/锁定结果
     */
    private void processSubOrders(OrderDraft orderDraft, OrderContext context, CouponOrderCalcResp calc) {
        for (CouponSubOrderResult subResult : calc.getSubOrders()) {
            OrderSubDO sub = Asserts.requireNonNull(orderDraft.subsByShop().get(subResult.getShopId()));
            updateSubAmounts(sub.getId(), subResult);
            insertItems(sub, subResult, context);
        }
    }

    /**
     * 标记父订单为已取消（幂等，供 Saga 补偿与超时恢复使用）。
     *
     * @param orderId 父订单 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void markCancelled(Long orderId) {
        orderMapper.update(
                Wrappers.<OrderDO>lambdaUpdate()
                        .set(OrderDO::getStatus, ParentOrderStatusEnum.CANCELLED)
                        .eq(OrderDO::getId, orderId)
                        .in(
                                OrderDO::getStatus,
                                ParentOrderStatusEnum.CREATING,
                                ParentOrderStatusEnum.PENDING_PAYMENT
                        )
        );
    }

    /**
     * 更新父订单金额。
     *
     * @param orderId 父订单 ID
     * @param calc 优惠计算/锁定结果
     */
    private void updateParentAmounts(Long orderId, CouponOrderCalcResp calc) {
        orderMapper.update(
                Wrappers.<OrderDO>lambdaUpdate()
                        .set(OrderDO::getTotalOriginalAmount, calc.getTotalOriginalAmount())
                        .set(OrderDO::getTotalShopDiscountAmount, calc.getTotalShopDiscountAmount())
                        .set(OrderDO::getTotalPlatformDiscountAmount, calc.getTotalPlatformDiscountAmount())
                        .set(OrderDO::getGoodsPayableAmount, calc.getGoodsPayableAmount())
                        .set(OrderDO::getPayableAmount, calc.getGoodsPayableAmount())
                        .eq(OrderDO::getId, orderId)
        );
    }

    /**
     * 更新子订单金额。
     *
     * @param subOrderId 子订单 ID
     * @param subResult 子订单优惠结果
     */
    private void updateSubAmounts(Long subOrderId, CouponSubOrderResult subResult) {
        orderSubMapper.update(
                Wrappers.<OrderSubDO>lambdaUpdate()
                        .set(OrderSubDO::getOriginalAmount, subResult.getOriginalAmount())
                        .set(OrderSubDO::getShopDiscountAmount, subResult.getShopDiscountAmount())
                        .set(OrderSubDO::getPlatformDiscountAmount, subResult.getPlatformDiscountAmount())
                        .set(OrderSubDO::getPayableAmount, subResult.getPayableAmount())
                        .set(OrderSubDO::getMerchantSettlementAmount, subResult.getPayableAmount())
                        .eq(OrderSubDO::getId, subOrderId)
        );
    }

    /**
     * 写入子订单明细（商品快照 + 优惠分摊快照）。
     *
     * @param sub 子订单
     * @param subResult 子订单优惠结果
     * @param context 订单上下文
     */
    private void insertItems(OrderSubDO sub, CouponSubOrderResult subResult, OrderContext context) {
        List<OrderItemDO> items = new ArrayList<>();
        for (CouponItemResult itemResult : subResult.getItems()) {
            SkuResp sku = context.getSkuMap().get(itemResult.getSkuId());
            if (sku == null) {
                continue;
            }
            items.add(
                    OrderItemDO.builder()
                               .subOrderId(sub.getId())
                               .productId(itemResult.getProductId())
                               .skuId(itemResult.getSkuId())
                               .shopId(sub.getShopId())
                               .categoryId(sku.getCategoryId())
                               .productName(sku.getProductName())
                               .productImage(sku.getImage())
                               .specSnapshot(sku.getSpecDesc())
                               .unitPrice(sku.getPrice())
                               .quantity(context.getQuantities().getOrDefault(itemResult.getSkuId(), 1))
                               .originalAmount(itemResult.getOriginalAmount())
                               .shopDiscountAmount(itemResult.getShopDiscountAmount())
                               .platformDiscountAmount(itemResult.getPlatformDiscountAmount())
                               .payableAmount(itemResult.getPayableAmount())
                               .build()
            );
        }
        if (CollUtil.isNotEmpty(items)) {
            orderItemMapper.insert(items);
        }
    }

    /**
     * 写入订单优惠关联快照（平台券关联父订单，店铺券关联对应子订单）。
     *
     * @param orderDraft 骨架
     * @param usages 实际使用的券快照
     */
    private void insertOrderCoupons(OrderDraft orderDraft, List<CouponUsageResult> usages) {
        if (CollUtil.isEmpty(usages)) {
            return;
        }
        List<OrderCouponDO> couponDOs = new ArrayList<>();
        for (CouponUsageResult usage : usages) {
            OrderSubDO orderSubDO = orderDraft.subsByShop().get(usage.getShopId());
            if (usage.getOwnerType() == CouponOwnerEnum.SHOP) {
                Asserts.notNull(orderSubDO);
            }
            OrderCouponDO orderCouponDO = orderCouponConverter.toCouponDO(
                    usage,
                    orderDraft.order().getId(),
                    orderSubDO.getId()
            );
            couponDOs.add(orderCouponDO);
        }
        orderCouponMapper.insert(couponDOs);
    }

    /**
     * 父订单迁移为待支付（条件更新防并发）。
     *
     * @param orderId 父订单 ID
     */
    private void transitionToPendingPayment(Long orderId) {
        int result = orderMapper.update(
                Wrappers.<OrderDO>lambdaUpdate()
                        .set(OrderDO::getStatus, ParentOrderStatusEnum.PENDING_PAYMENT)
                        .eq(OrderDO::getId, orderId)
                        .eq(OrderDO::getStatus, ParentOrderStatusEnum.CREATING)
        );
        Asserts.isTrue(result > 0, BizError.INVALID_ORDER_STATUS);
    }
}
