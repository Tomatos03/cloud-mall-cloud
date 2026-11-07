package com.cloudmall.coupon.calc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.coupon.api.model.bo.CouponCalcItem;
import com.cloudmall.coupon.api.model.resp.CouponItemResult;
import com.cloudmall.coupon.api.model.resp.CouponOrderCalcResp;
import com.cloudmall.coupon.api.model.resp.CouponSubOrderResult;
import com.cloudmall.coupon.api.model.resp.CouponUsageResult;
import com.cloudmall.coupon.convert.CouponOrderConverter;

/**
 * 优惠计算器
 * <p>
 * 纯计算核心，不访问数据库。计算顺序：先店铺券、后平台券；
 * 优惠按适用明细金额比例分摊，尾差归最大金额项；中间计算不截断，
 * 最终金额 2 位小数 HALF_UP，并校验三级金额合计一致。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class CouponCalculator {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final CouponOrderConverter couponOrderConverter;

    /**
     * 计算父订单/子订单/明细三级优惠。
     *
     * @param allItems 全部商品明细（skuId 唯一）
     * @param platformCoupon 平台券（可空）
     * @param shopCoupons 店铺券列表（每店最多一张，可空）
     * @return 优惠快照
     */
    public CouponOrderCalcResp calculate(
        List<CouponCalcItem> allItems,
        CalcCoupon platformCoupon,
        List<CalcCoupon> shopCoupons
    ) {
        Map<Long, Integer> skuIndex = buildSkuIndex(allItems);
        BigDecimal[] shopDiscount = new BigDecimal[allItems.size()];
        BigDecimal[] platformDiscount = new BigDecimal[allItems.size()];
        for (int i = 0; i < allItems.size(); i++) {
            shopDiscount[i] = BigDecimal.ZERO;
            platformDiscount[i] = BigDecimal.ZERO;
        }

        List<CouponUsageResult> usages = new ArrayList<>();
        for (CalcCoupon coupon : CollUtil.emptyIfNull(shopCoupons)) {
            BigDecimal applicableAmount = sumLineAmount(coupon.getApplicableItems());
            assertMeetsThreshold(coupon, applicableAmount);
            BigDecimal discount = applyFormula(coupon, applicableAmount);
            allocateAndAdd(discount, coupon.getApplicableItems(), skuIndex, shopDiscount);
            usages.add(couponOrderConverter.toUsage(coupon, applicableAmount, discount));
        }

        if (platformCoupon != null) {
            List<CouponCalcItem> applicable = platformCoupon.getApplicableItems();
            BigDecimal platformBase = sumBaseAmount(applicable, skuIndex, shopDiscount);
            assertMeetsThreshold(platformCoupon, platformBase);
            BigDecimal discount = applyFormula(platformCoupon, platformBase);
            allocatePlatformDiscount(discount, applicable, skuIndex, platformDiscount);
            usages.add(couponOrderConverter.toUsage(platformCoupon, platformBase, discount));
        }
        return buildResp(allItems, skuIndex, shopDiscount, platformDiscount, usages);
    }

    /**
     * 构建 sku 唯一索引。
     *
     * @param items 商品明细列表
     * @return skuId → 下标
     */
    private Map<Long, Integer> buildSkuIndex(List<CouponCalcItem> items) {
        Map<Long, Integer> index = new LinkedHashMap<>();
        for (int i = 0; i < items.size(); i++) {
            index.put(items.get(i).getSkuId(), i);
        }
        return index;
    }

    /**
     * 累加明细行金额。
     *
     * @param items 明细列表
     * @return 合计
     */
    private BigDecimal sumLineAmount(List<CouponCalcItem> items) {
        if (CollUtil.isEmpty(items)) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                    .map(CouponCalcItem::getLineAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算店铺券处理后的适用金额（原始金额 - 店铺优惠）。
     *
     * @param items 明细列表
     * @param skuIndex sku 索引
     * @param shopDiscount 店铺优惠数组
     * @return 合计
     */
    private BigDecimal sumBaseAmount(
        List<CouponCalcItem> items,
        Map<Long, Integer> skuIndex,
        BigDecimal[] shopDiscount
    ) {
        if (CollUtil.isEmpty(items)) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (CouponCalcItem item : items) {
            int idx = skuIndex.get(item.getSkuId());
            total = total.add(item.getLineAmount().subtract(shopDiscount[idx]));
        }
        return total;
    }

    /**
     * 校验门槛：threshold 为空或 0 表示无门槛。
     *
     * @param coupon 券
     * @param applicableAmount 适用金额
     */
    private void assertMeetsThreshold(CalcCoupon coupon, BigDecimal applicableAmount) {
        Asserts.isTrue(coupon.isMeetsThreshold(applicableAmount), BizErrorEnum.COUPON_QUOTE_INVALID);
    }

    /**
     * 按券类型计算优惠金额。
     *
     * @param coupon 券
     * @param applicableAmount 适用金额
     * @return 优惠金额
     */
    private BigDecimal applyFormula(CalcCoupon coupon, BigDecimal applicableAmount) {
        return coupon.getType()
                     .calcDiscount(
                             applicableAmount,
                             coupon.getDiscountAmount(),
                             coupon.getDiscountRate(),
                             SCALE,
                             ROUNDING
                     );
    }

    /**
     * 按适用明细金额比例分摊并累加到明细优惠数组，尾差归最大金额明细。
     *
     * @param total 优惠总额
     * @param items 适用明细
     * @param skuIndex sku 索引
     * @param target 累加目标数组
     */
    private void allocateAndAdd(
        BigDecimal total,
        List<CouponCalcItem> items,
        Map<Long, Integer> skuIndex,
        BigDecimal[] target
    ) {
        List<BigDecimal> amounts = items.stream()
                                        .map(CouponCalcItem::getLineAmount)
                                        .toList();
        List<BigDecimal> parts = allocateByAmount(total, amounts);
        for (int i = 0; i < items.size(); i++) {
            int idx = skuIndex.get(items.get(i).getSkuId());
            target[idx] = target[idx].add(parts.get(i));
        }
    }

    /**
     * 平台券优惠分摊：先按子订单适用金额比例分摊（尾差归最大子订单）， 再按子订单内明细金额比例分摊（尾差
     * 归最大明细）。
     *
     * @param total 平台优惠总额
     * @param applicable 平台适用明细
     * @param skuIndex sku 索引
     * @param target 明细平台优惠数组
     */
    private void allocatePlatformDiscount(
        BigDecimal total,
        List<CouponCalcItem> applicable,
        Map<Long, Integer> skuIndex,
        BigDecimal[] target
    ) {
        Map<Long, List<CouponCalcItem>> byShop = groupByShop(applicable);
        List<Long> shopIds = new ArrayList<>(byShop.keySet());
        List<BigDecimal> subBaseAmounts = shopIds.stream()
                                                 .map(shopId -> sumLineAmount(byShop.get(shopId)))
                                                 .toList();
        List<BigDecimal> subParts = allocateByAmount(total, subBaseAmounts);
        for (int i = 0; i < shopIds.size(); i++) {
            allocateAndAdd(subParts.get(i), byShop.get(shopIds.get(i)), skuIndex, target);
        }
    }

    /**
     * 按店铺分组商品明细（保持店铺出现顺序）。
     *
     * @param items 明细列表
     * @return 店铺 ID → 明细列表
     */
    private Map<Long, List<CouponCalcItem>> groupByShop(List<CouponCalcItem> items) {
        return items.stream()
                    .collect(
                            Collectors.groupingBy(
                                    CouponCalcItem::getShopId,
                                    LinkedHashMap::new,
                                    Collectors.toList()
                            )
                    );
    }

    /**
     * 按金额比例分摊，尾差归最大金额元素（相同金额归首个）。
     *
     * @param total 总额
     * @param amounts 各元素金额
     * @return 分摊结果（与 amounts 等长）
     */
    private List<BigDecimal> allocateByAmount(BigDecimal total, List<BigDecimal> amounts) {
        int size = amounts.size();
        List<BigDecimal> result = new ArrayList<>(Collections.nCopies(size, BigDecimal.ZERO));
        if (size == 0 || total.signum() <= 0) {
            return result;
        }
        BigDecimal sum = amounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.signum() <= 0) {
            return result;
        }
        BigDecimal remaining = total;
        int maxIdx = 0;
        BigDecimal maxAmount = amounts.get(0);
        for (int i = 0; i < size; i++) {
            BigDecimal part = amounts.get(i)
                                     .multiply(total)
                                     .divide(sum, SCALE, ROUNDING);
            result.set(i, part);
            remaining = remaining.subtract(part);
            if (amounts.get(i).compareTo(maxAmount) > 0) {
                maxAmount = amounts.get(i);
                maxIdx = i;
            }
        }
        result.set(maxIdx, result.get(maxIdx).add(remaining));
        return result;
    }

    /**
     * 汇总三级金额并校验合计一致。
     *
     * @param allItems 全部明细
     * @param skuIndex sku 索引
     * @param shopDiscount 店铺优惠数组
     * @param platformDiscount 平台优惠数组
     * @param usages 使用券快照
     * @return 优惠快照
     */
    private CouponOrderCalcResp buildResp(
        List<CouponCalcItem> allItems,
        Map<Long, Integer> skuIndex,
        BigDecimal[] shopDiscount,
        BigDecimal[] platformDiscount,
        List<CouponUsageResult> usages
    ) {
        List<CouponItemResult> itemResults = new ArrayList<>();
        for (CouponCalcItem item : allItems) {
            int idx = skuIndex.get(item.getSkuId());
            BigDecimal itemShop = shopDiscount[idx];
            BigDecimal itemPlatform = platformDiscount[idx];
            assertNotNegative(item.getLineAmount()
                                  .subtract(itemShop)
                                  .subtract(itemPlatform));
            itemResults.add(couponOrderConverter.toItemResult(item, itemShop, itemPlatform));
        }

        List<CouponSubOrderResult> subOrders = new ArrayList<>();
        Map<Long, List<CouponItemResult>> byShop = itemResults.stream()
                                                              .collect(
                                                                      Collectors.groupingBy(
                                                                              item -> findShopId(
                                                                                      allItems,
                                                                                      item.getSkuId()
                                                                              ),
                                                                              LinkedHashMap::new,
                                                                              Collectors.toList()
                                                                      )
                                                              );
        for (Map.Entry<Long, List<CouponItemResult>> entry : byShop.entrySet()) {
            subOrders.add(couponOrderConverter.toSubOrderResult(entry.getKey(), entry.getValue()));
        }

        CouponOrderCalcResp resp = couponOrderConverter.toResp(itemResults, subOrders, usages);
        assertNotNegative(resp.getProductPayableAmount());
        return resp;
    }

    /**
     * 根据 sku 定位商品所属店铺。
     *
     * @param allItems 全部明细
     * @param skuId sku ID
     * @return 店铺 ID
     */
    private Long findShopId(List<CouponCalcItem> allItems, Long skuId) {
        return allItems.stream()
                       .filter(item -> item.getSkuId().equals(skuId))
                       .findFirst()
                       .map(CouponCalcItem::getShopId)
                       .orElse(null);
    }

    /**
     * 金额不得为负。
     *
     * @param amount 金额
     */
    private void assertNotNegative(BigDecimal amount) {
        if (amount.signum() < 0) {
            throw new BizException(BizErrorEnum.COUPON_QUOTE_INVALID);
        }
    }
}
