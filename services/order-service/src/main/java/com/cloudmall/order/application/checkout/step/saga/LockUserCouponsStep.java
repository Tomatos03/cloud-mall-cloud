package com.cloudmall.order.application.checkout.step.saga;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cloudmall.coupon.api.client.InternalCouponClient;
import com.cloudmall.coupon.api.model.dto.CouponCalcItem;
import com.cloudmall.coupon.api.model.req.CouponOrderLockReq;
import com.cloudmall.coupon.api.model.req.CouponOrderReleaseReq;
import com.cloudmall.coupon.api.model.resp.CouponOrderCalcResp;
import com.cloudmall.coupon.api.model.resp.CouponUsageResult;
import com.cloudmall.order.model.bo.CheckoutContext;
import com.cloudmall.product.api.model.resp.SkuResp;

/**
 * 锁定用户优惠券子步骤
 * <p>
 * 按服务端可信快照重新计算并锁定用户券；锁券结果写入上下文供落库步骤使用，
 * 已锁券快照供逆序补偿逐张释放（逐张尽力而为，单张失败不阻断其它券）。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LockUserCouponsStep implements ISagaSteps {

    /** 锁券过期时长（订单支付超时窗口） */
    private static final int LOCK_EXPIRE_MINUTES = 30;

    /** 补偿释放原因 */
    private static final String COMPENSATE_REASON = "下单失败补偿释放";

    private final InternalCouponClient internalCouponClient;

    @Override
    public CheckoutSagaStepsEnum name() {
        return CheckoutSagaStepsEnum.LOCK_USER_COUPONS;
    }

    @Override
    public void execute(CheckoutContext context) {
        CouponOrderCalcResp calc = lockCoupons(context);
        context.setCouponCalc(calc);
        context.setLockedUsages(calc.getUsages());
    }

    @Override
    public void compensate(CheckoutContext context) {
        if (CollUtil.isEmpty(context.getLockedUsages())) {
            return;
        }
        for (CouponUsageResult usage : context.getLockedUsages()) {
            try {
                internalCouponClient.release(
                        CouponOrderReleaseReq.builder()
                                             .userId(context.getUserId())
                                             .orderNo(context.getOrder().getOrderNo())
                                             .userCouponId(usage.getUserCouponId())
                                             .reason(COMPENSATE_REASON)
                                             .build()
                );
            } catch (RuntimeException e) {
                log.warn(
                        "Saga 补偿释放用户券失败 orderNo={} userCouponId={}",
                        context.getOrder().getOrderNo(),
                        usage.getUserCouponId(),
                        e
                );
            }
        }
    }

    /**
     * 调用优惠券服务重新计算并锁定用户券。
     *
     * @param context 订单上下文
     * @return 优惠快照
     */
    private CouponOrderCalcResp lockCoupons(CheckoutContext context) {
        CouponOrderLockReq lockReq = CouponOrderLockReq
                .builder()
                .orderNo(context.getOrder().getOrderNo())
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
        return CollStreamUtil.toList(
                checkoutItems.entrySet(),
                entry -> {
                    SkuResp sku = skuMap.get(entry.getKey());
                    BigDecimal lineAmount = sku.getPrice().multiply(BigDecimal.valueOf(entry.getValue()));
                    return CouponCalcItem.builder()
                                         .skuId(sku.getSkuId())
                                         .productId(sku.getProductId())
                                         .categoryId(sku.getCategoryId())
                                         .shopId(sku.getShopId())
                                         .unitPrice(sku.getPrice())
                                         .quantity(entry.getValue())
                                         .lineAmount(lineAmount)
                                         .build();
                }
        );
    }
}
