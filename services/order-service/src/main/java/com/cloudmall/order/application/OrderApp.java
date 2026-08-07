package com.cloudmall.order.application;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.coupon.api.client.CouponOrderClient;
import com.cloudmall.coupon.api.dto.CouponCalcItem;
import com.cloudmall.coupon.api.request.CouponOrderQuoteReq;
import com.cloudmall.coupon.api.response.CouponOrderCalcResp;
import com.cloudmall.order.api.enums.CheckoutTypeEnum;
import com.cloudmall.order.api.model.OrderItemReq;
import com.cloudmall.order.api.request.CreateOrderReq;
import com.cloudmall.order.api.request.QuoteOrderReq;
import com.cloudmall.product.api.client.SkuClient;
import com.cloudmall.product.api.response.SkuResp;

/**
 * 订单应用服务
 * <p>
 * 统一建单入口：按路径参数中的结算类型委托结算管道执行
 * （购物车结算、直接购买结算、秒杀结算），并保留下单前价格预览。
 * 本地查询与事务写库见 {@link com.cloudmall.order.service.impl.OrderService}。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class OrderApp {

    private final CheckoutPipeline checkoutPipeline;
    private final CouponOrderClient couponOrderClient;
    private final SkuClient skuClient;

    /**
     * 创建订单（统一入口，按结算类型执行结算管道）。
     *
     * @param checkoutType 结算类型（路径参数）
     * @param userId 当前登录用户 ID（秒杀内部调用可为 null，由秒杀快照写入）
     * @param req 建单请求
     * @return 订单编号
     */
    public String create(CheckoutTypeEnum checkoutType, Long userId, CreateOrderReq req) {
        return checkoutPipeline.execute(checkoutType, userId, req);
    }

    /**
     * 下单前价格预览（无副作用）。
     *
     * @param req 预览请求（只需商品 SKU 与数量）
     * @return 三级价格明细 + 优惠券使用明细 + 推荐组合
     */
    public CouponOrderCalcResp quote(QuoteOrderReq req) {
        Long userId = Asserts.requireNonNull(UserContextHolder.getUserId(), BizError.USER_NOT_AUTHENTICATED);
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderItemReq item : req.getItems()) {
            Asserts.isFalse(quantities.containsKey(item.getSkuId()), BizError.INVALID_PARAM);
            quantities.put(item.getSkuId(), item.getQuantity());
        }
        Map<Long, SkuResp> skuMap = requireAvailableSkus(quantities);
        List<CouponCalcItem> calcItems = buildCalcItems(quantities, skuMap);
        return couponOrderClient.quote(
                CouponOrderQuoteReq.builder()
                                   .userId(userId)
                                   .items(calcItems)
                                   .userCouponIds(req.getUserCouponIds())
                                   .build()
        );
    }

    /**
     * 批量获取可信 SKU 快照并校验上下架与库存。
     *
     * @param quantities skuId → 数量
     * @return SKU 可信快照 map
     */
    private Map<Long, SkuResp> requireAvailableSkus(Map<Long, Integer> quantities) {
        List<SkuResp> skus = skuClient.listSkusByIds(new ArrayList<>(quantities.keySet()));
        Asserts.notEmpty(skus, BizError.ORDER_CREATE_SKU_NOT_AVAILABLE);
        Map<Long, SkuResp> skuMap = skus.stream()
                                        .collect(Collectors.toMap(SkuResp::getSkuId, Function.identity()));
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            SkuResp sku = Asserts.requireNonNull(
                    skuMap.get(entry.getKey()),
                    BizError.ORDER_CREATE_SKU_NOT_AVAILABLE
            );
            Asserts.isTrue(
                    sku.getStock() != null && sku.getStock() >= entry.getValue(),
                    BizError.ORDER_CREATE_SKU_INVENTORY_NOT_ENOUGH
            );
        }
        return skuMap;
    }

    /**
     * 组装可信商品明细（金额由服务端计算，不信任客户端）。
     *
     * @param quantities skuId → 数量
     * @param skuMap SKU 可信快照
     * @return 商品明细列表
     */
    private static List<CouponCalcItem> buildCalcItems(
            Map<Long, Integer> quantities,
            Map<Long, SkuResp> skuMap
    ) {
        List<CouponCalcItem> items = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
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
}
