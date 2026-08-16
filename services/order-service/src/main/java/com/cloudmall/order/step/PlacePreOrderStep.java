package com.cloudmall.order.step;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cloudmall.coupon.api.client.InternalCouponClient;
import com.cloudmall.coupon.api.dto.CouponCalcItem;
import com.cloudmall.coupon.api.request.CouponOrderLockReq;
import com.cloudmall.coupon.api.request.CouponOrderReleaseReq;
import com.cloudmall.coupon.api.response.CouponOrderCalcResp;
import com.cloudmall.coupon.api.response.CouponUsageResult;
import com.cloudmall.order.entity.OrderDO;
import com.cloudmall.order.model.CheckoutContext;
import com.cloudmall.order.service.impl.OrderService;
import com.cloudmall.product.api.client.InternalProductClient;
import com.cloudmall.product.api.request.StockOpReq;
import com.cloudmall.product.api.request.StockReserveItem;
import com.cloudmall.product.api.response.SkuResp;

/**
 * 预订单落库步骤
 * <p>
 * 购物车结算与直接购买结算共享：CREATING 骨架 → 预占库存 → 锁券 → 落库金额/明细/优惠 → 待支付，
 * 任一环节失败按逆序补偿并上抛，成功后将父订单写入 context。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlacePreOrderStep implements CheckoutStep {

    /** 锁券过期时长（订单支付超时窗口） */
    private static final int LOCK_EXPIRE_MINUTES = 30;

    /** 下单预占库存原因 */
    private static final String RESERVE_REASON = "下单预占库存";

    /** 补偿释放原因 */
    private static final String COMPENSATE_REASON = "下单失败补偿释放";

    private final OrderService orderService;
    private final InternalProductClient internalProductClient;
    private final InternalCouponClient internalCouponClient;

    /**
     * 步骤名称。
     *
     * @return 预订单落库
     */
    @Override
    public CheckoutStepEnum name() {
        return CheckoutStepEnum.PLACE_PRE_ORDER;
    }

    /**
     * 执行步骤：创建预订单并执行预占式 Saga，成功写入 context.order。
     *
     * @param ctx 订单上下文
     */
    @Override
    public void process(CheckoutContext ctx) {
        OrderDO order = orderService.createPreOrder(ctx);
        String orderNo = order.getOrderNo();
        boolean stockReserved = false;
        List<CouponUsageResult> lockedUsages = Collections.emptyList();
        try {
            internalProductClient.reserve(buildStockOpReq(orderNo, ctx.getCheckoutItems(), RESERVE_REASON));
            stockReserved = true;
            CouponOrderCalcResp calc = lockCoupons(ctx, orderNo);
            lockedUsages = calc.getUsages();
            orderService.finalizeOrder(order, ctx, calc);
        } catch (RuntimeException e) {
            compensate(order, ctx, orderNo, stockReserved, lockedUsages);
            throw e;
        }
        ctx.setOrder(order);
        log.info("下单成功 orderNo={} orderId={}", order.getOrderNo(), order.getId());
    }

    /**
     * 调用优惠券服务重新计算并锁定用户券。
     *
     * @param context 订单上下文
     * @param orderNo 订单号
     * @return 优惠快照
     */
    private CouponOrderCalcResp lockCoupons(CheckoutContext context, String orderNo) {
        CouponOrderLockReq lockReq = CouponOrderLockReq
                .builder()
                .orderNo(orderNo)
                .lockExpireTime(LocalDateTime.now().plusMinutes(LOCK_EXPIRE_MINUTES))
                .userId(context.getUserId())
                .items(buildCalcItems(context.getCheckoutItems(), context.getSkuMap()))
                .userCouponIds(context.getReq().getUserCouponIds())
                .build();
        return internalCouponClient.lock(lockReq);
    }

    /**
     * 组装可信商品明细（金额由服务端计算，不信任客户端）。
     *
     * @param checkoutItems skuId → 下单数量
     * @param skuMap SKU 可信快照
     * @return 商品明细列表
     */
    private static List<CouponCalcItem> buildCalcItems(
        Map<Long, Integer> checkoutItems,
        Map<Long, SkuResp> skuMap
    ) {
        List<CouponCalcItem> items = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : checkoutItems.entrySet()) {
            SkuResp sku = skuMap.get(entry.getKey());
            BigDecimal lineAmount = sku.getPrice().multiply(BigDecimal.valueOf(entry.getValue()));
            items.add(
                    CouponCalcItem.builder()
                                  .skuId(sku.getSkuId())
                                  .productId(sku.getProductId())
                                  .categoryId(sku.getCategoryId())
                                  .shopId(sku.getShopId())
                                  .unitPrice(sku.getPrice())
                                  .quantity(entry.getValue())
                                  .lineAmount(lineAmount)
                                  .build()
            );
        }
        return items;
    }

    /**
     * Saga 补偿：先释放已锁用户券，再释放库存，最后标记订单取消。每个动作尽力而为且幂等，失败由超时恢
     * 复任务兜底。
     *
     * @param order 父订单
     * @param context 订单上下文
     * @param orderNo 订单号
     * @param stockReserved 是否已预占库存
     * @param lockedUsages 已锁定的券快照
     */
    private void compensate(
        OrderDO order,
        CheckoutContext context,
        String orderNo,
        boolean stockReserved,
        List<CouponUsageResult> lockedUsages
    ) {
        releaseCouponsQuietly(context, orderNo, lockedUsages);
        if (stockReserved) {
            releaseStockQuietly(context, orderNo);
        }
        try {
            orderService.markCancelled(order.getId());
        } catch (RuntimeException e) {
            log.warn("Saga 补偿标记订单取消失败 orderNo={}", orderNo, e);
        }
    }

    /**
     * 释放已锁定的用户券（尽力而为，幂等）。
     *
     * @param context 订单上下文
     * @param orderNo 订单号
     * @param lockedUsages 已锁定的券快照
     */
    private void releaseCouponsQuietly(
        CheckoutContext context,
        String orderNo,
        List<CouponUsageResult> lockedUsages
    ) {
        if (CollUtil.isEmpty(lockedUsages)) {
            return;
        }
        for (CouponUsageResult usage : lockedUsages) {
            try {
                internalCouponClient.release(
                        CouponOrderReleaseReq.builder()
                                             .userId(context.getUserId())
                                             .orderNo(orderNo)
                                             .userCouponId(usage.getUserCouponId())
                                             .reason(COMPENSATE_REASON)
                                             .build()
                );
            } catch (RuntimeException e) {
                log.warn("Saga 补偿释放用户券失败 orderNo={} userCouponId={}", orderNo, usage.getUserCouponId(), e);
            }
        }
    }

    /**
     * 释放预占库存（尽力而为，幂等）。
     *
     * @param context 订单上下文
     * @param orderNo 订单号
     */
    private void releaseStockQuietly(CheckoutContext context, String orderNo) {
        try {
            internalProductClient.release(
                    buildStockOpReq(orderNo, context.getCheckoutItems(), COMPENSATE_REASON)
            );
        } catch (RuntimeException e) {
            log.warn("Saga 补偿释放库存失败 orderNo={}", orderNo, e);
        }
    }

    /**
     * 组装库存操作请求。
     *
     * @param orderNo 订单号
     * @param quantities skuId → 数量
     * @param reason 原因
     * @return 库存操作请求
     */
    private StockOpReq buildStockOpReq(String orderNo, Map<Long, Integer> quantities, String reason) {
        List<StockReserveItem> items = quantities
                .entrySet()
                .stream()
                .map(
                        entry -> StockReserveItem.builder()
                                                 .skuId(entry.getKey())
                                                 .quantity(entry.getValue())
                                                 .build()
                )
                .toList();
        return StockOpReq.builder()
                         .orderNo(orderNo)
                         .items(items)
                         .reason(reason)
                         .build();
    }
}
