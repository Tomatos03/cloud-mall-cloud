package com.cloudmall.order.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.coupon.api.client.CouponOrderClient;
import com.cloudmall.coupon.api.dto.CouponCalcItem;
import com.cloudmall.coupon.api.request.CouponOrderLockReq;
import com.cloudmall.coupon.api.request.CouponOrderQuoteReq;
import com.cloudmall.coupon.api.request.CouponOrderReleaseReq;
import com.cloudmall.coupon.api.response.CouponOrderCalcResp;
import com.cloudmall.coupon.api.response.CouponUsageResult;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.order.api.model.OrderItemReq;
import com.cloudmall.order.api.request.CreateOrderReq;
import com.cloudmall.order.api.request.QuoteOrderReq;
import com.cloudmall.order.api.response.OrderResp;
import com.cloudmall.order.api.response.OrderSubResp;
import com.cloudmall.order.convert.OrderConverter;
import com.cloudmall.order.convert.OrderCouponConverter;
import com.cloudmall.order.convert.OrderItemConverter;
import com.cloudmall.order.convert.OrderSubConverter;
import com.cloudmall.order.entity.CartDO;
import com.cloudmall.order.entity.OrderCouponDO;
import com.cloudmall.order.entity.OrderDO;
import com.cloudmall.order.entity.OrderItemDO;
import com.cloudmall.order.entity.OrderSubDO;
import com.cloudmall.order.mapper.OrderCouponMapper;
import com.cloudmall.order.mapper.OrderItemMapper;
import com.cloudmall.order.mapper.OrderMapper;
import com.cloudmall.order.mapper.OrderSubMapper;
import com.cloudmall.order.model.OrderContext;
import com.cloudmall.order.model.OrderDraft;
import com.cloudmall.order.model.req.OrderPageReq;
import com.cloudmall.order.model.req.OrderSubPageReq;
import com.cloudmall.order.service.ICartService;
import com.cloudmall.order.service.IOrderService;
import com.cloudmall.product.api.client.SkuClient;
import com.cloudmall.product.api.client.StockClient;
import com.cloudmall.product.api.request.StockOpReq;
import com.cloudmall.product.api.request.StockReserveItem;
import com.cloudmall.product.api.response.SkuResp;
import com.cloudmall.user.api.client.AddressClient;
import com.cloudmall.user.api.response.AddressResp;

/**
 * 订单服务实现
 * <p>
 * 下单 Saga 分段提交：CREATING 骨架独立提交 → 远程预占库存与锁券 →
 * 金额/明细/优惠落库 → 待支付。失败按逆序释放券与库存并标记取消；
 * 中途宕机遗留的 CREATING 订单由超时恢复任务兜底。
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    /** 锁券过期时长（订单支付超时窗口） */
    private static final int LOCK_EXPIRE_MINUTES = 30;

    /** 下单预占库存原因 */
    private static final String RESERVE_REASON = "下单预占库存";

    /** 补偿释放原因 */
    private static final String COMPENSATE_REASON = "下单失败补偿释放";

    private final OrderMapper orderMapper;
    private final OrderSubMapper orderSubMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderCouponMapper orderCouponMapper;
    private final OrderConverter orderConverter;
    private final OrderSubConverter orderSubConverter;
    private final OrderItemConverter orderItemConverter;
    private final OrderCouponConverter orderCouponConverter;
    private final OrderPersistenceService orderPersistence;
    private final ICartService cartService;
    private final AddressClient addressClient;
    private final SkuClient skuClient;
    private final StockClient stockClient;
    private final CouponOrderClient couponOrderClient;

    /**
     * 创建订单（跨店结算 Saga）。
     *
     * @param req 下单请求
     * @return 父订单详情
     */
    @Override
    public OrderResp create(CreateOrderReq req) {
        // 准备订单上下文：用户、地址、数量、SKU 快照与计价明细
        OrderContext context = prepareOrderContext(req);

        // 创建 CREATING 草稿单
        OrderDraft draft = initializeDraft(context);

        // 预占库存、锁定优惠券并最终落库，失败自动补偿
        OrderDO finalizedOrder = executePlacement(draft, context);

        // 清理购物车并组装响应
        cleanupAfterPlacement(context, finalizedOrder);
        return buildResponse(finalizedOrder);
    }

    /**
     * 准备订单上下文：折叠用户、地址、数量合并、购物车校验、SKU 快照、计价明细与店铺拆分。
     *
     * @param req 下单请求
     * @return 订单上下文
     */
    private OrderContext prepareOrderContext(CreateOrderReq req) {
        Long userId = requireUserId();
        AddressResp address = requireAddress(userId, req.getAddressId());
        Map<Long, Integer> quantities = mergeQuantities(req.getItems());
        requireCartSelection(userId, quantities);
        Map<Long, SkuResp> skuMap = requireAvailableSkus(quantities);
        List<CouponCalcItem> calcItems = buildCalcItems(quantities, skuMap);
        List<Long> shopIds = toShopIdList(skuMap);
        return OrderContext.builder()
                           .userId(userId)
                           .req(req)
                           .address(address)
                           .quantities(quantities)
                           .skuMap(skuMap)
                           .calcItems(calcItems)
                           .shopIds(shopIds)
                           .build();
    }

    /**
     * 创建草稿单：生成单号、序列化地址快照并持久化 CREATING 骨架。
     *
     * @param context 订单上下文
     * @return 下单骨架
     */
    private OrderDraft initializeDraft(OrderContext context) {
        String orderNo = genOrderNo();
        String addressSnapshot = JSONUtil.toJsonStr(context.getAddress());
        return orderPersistence.createDraft(
                context.getUserId(),
                orderNo,
                context.getReq(),
                addressSnapshot,
                context.getShopIds()
        );
    }

    /**
     * 执行下单核心：预占库存、锁定优惠券并最终落库，返回已迁移为待支付的父订单。 任一环节失败按逆序补偿并
     * 上抛，主流程不感知中间状态。
     *
     * @param draft 下单骨架
     * @param context 订单上下文
     * @return 最终父订单
     */
    private OrderDO executePlacement(OrderDraft draft, OrderContext context) {
        String orderNo = draft.order().getOrderNo();
        boolean stockReserved = false;
        List<CouponUsageResult> lockedUsages = Collections.emptyList();
        try {
            stockClient.reserve(buildStockOpReq(orderNo, context.getQuantities(), RESERVE_REASON));
            stockReserved = true;
            CouponOrderCalcResp calc = lockCoupons(
                    context.getUserId(),
                    orderNo,
                    context.getReq(),
                    context.getCalcItems()
            );
            lockedUsages = calc.getUsages();
            orderPersistence.finalizeOrder(draft, calc, context.getSkuMap(), context.getQuantities());
        } catch (RuntimeException e) {
            compensate(
                    draft,
                    context.getUserId(),
                    orderNo,
                    context.getQuantities(),
                    stockReserved,
                    lockedUsages
            );
            throw e;
        }
        return orderMapper.selectById(draft.order().getId());
    }

    /**
     * 清理购物车中已下单的商品并记录成功日志。
     *
     * @param context 订单上下文
     * @param finalizedOrder 最终父订单
     */
    private void cleanupAfterPlacement(OrderContext context, OrderDO finalizedOrder) {
        List<Long> skuIdList = context.getQuantities()
                                      .keySet()
                                      .stream()
                                      .toList();
        cartService.removeItems(context.getUserId(), skuIdList);
        log.info("下单成功 orderNo={} orderId={}", finalizedOrder.getOrderNo(), finalizedOrder.getId());
    }

    /**
     * 组装父订单响应。
     *
     * @param finalizedOrder 最终父订单
     * @return 父订单详情
     */
    private OrderResp buildResponse(OrderDO finalizedOrder) {
        return toResp(finalizedOrder);
    }

    @NonNull
    private static List<Long> toShopIdList(Map<Long, SkuResp> skuMap) {
        return skuMap.values()
                     .stream()
                     .map(SkuResp::getShopId)
                     .toList();
    }

    private static String genOrderNo() {
        return StrUtil.sub(IdUtil.fastSimpleUUID(), 0, 20);
    }

    /**
     * 下单前价格预览（无副作用）。
     *
     * @param req 预览请求（只需商品 SKU 与数量）
     * @return 三级价格明细 + 优惠券使用明细 + 推荐组合
     */
    @Override
    public CouponOrderCalcResp quote(QuoteOrderReq req) {
        return couponOrderClient.quote(buildQuoteReq(req));
    }

    private CouponOrderQuoteReq buildQuoteReq(QuoteOrderReq req) {
        Long userId = requireUserId();
        Map<Long, Integer> quantities = mergeQuantities(req.getItems());
        Map<Long, SkuResp> skuMap = requireAvailableSkus(quantities);
        List<CouponCalcItem> calcItems = buildCalcItems(quantities, skuMap);
        return CouponOrderQuoteReq.builder()
                                  .userId(userId)
                                  .items(calcItems)
                                  .userCouponIds(req.getUserCouponIds())
                                  .build();
    }

    /**
     * 获取父订单详情（仅当前用户本人可查）。
     *
     * @param id 父订单 ID
     * @return 父订单详情
     */
    @Override
    public OrderResp getById(Long id) {
        OrderDO order = Asserts.requireNonNull(orderMapper.selectById(id), BizError.ORDER_NOT_EXIST);
        Long userId = UserContextHolder.getUserId();
        Asserts.isTrue(Objects.equals(order.getUserId(), userId), BizError.ORDER_NOT_BELONG_TO_USER);
        return toResp(order);
    }

    /**
     * 用户分页查询自己的父订单。
     *
     * @param req 分页请求
     * @return 父订单分页
     */
    @Override
    public IPage<OrderResp> pageUser(OrderPageReq req) {
        req.setUserId(requireUserId());
        IPage<OrderDO> page = orderMapper.selectPage(req, req.buildLambdaWrapper());
        return page.convert(this::toResp);
    }

    /**
     * 商家分页查询自己店铺的子订单。
     *
     * @param req 分页请求（shopId 由服务层注入）
     * @return 子订单分页
     */
    @Override
    public IPage<OrderSubResp> pageMerchant(OrderSubPageReq req) {
        Long shopId = Asserts.requireNonNull(UserContextHolder.getShopId(), BizError.MERCHANT_NO_SHOP);
        req.setShopId(shopId);
        IPage<OrderSubDO> page = orderSubMapper.selectPage(req, req.buildLambdaWrapper());
        return page.convert(this::toSubRespWithItems);
    }

    /**
     * 运营分页查询全量父订单。
     *
     * @param req 分页请求
     * @return 父订单分页
     */
    @Override
    public IPage<OrderResp> pageOperator(OrderPageReq req) {
        IPage<OrderDO> page = orderMapper.selectPage(req, req.buildLambdaWrapper());
        return page.convert(this::toResp);
    }

    /**
     * 组装父订单响应：子订单、明细与券快照。
     *
     * @param order 父订单实体
     * @return 父订单响应
     */
    private OrderResp toResp(OrderDO order) {
        OrderResp resp = orderConverter.toResp(order);
        List<OrderSubDO> subs = orderSubMapper.selectList(
                Wrappers.<OrderSubDO>lambdaQuery()
                        .eq(OrderSubDO::getParentOrderId, order.getId())
                        .orderByAsc(OrderSubDO::getId)
        );
        resp.setSubs(toSubRespsWithItems(subs));
        List<OrderCouponDO> coupons = orderCouponMapper.selectList(
                Wrappers.<OrderCouponDO>lambdaQuery().eq(OrderCouponDO::getParentOrderId, order.getId())
        );
        resp.setCoupons(orderCouponConverter.toRespList(coupons));
        return resp;
    }

    /**
     * 批量组装子订单响应并补充明细。
     *
     * @param subs 子订单实体列表
     * @return 子订单响应列表
     */
    private List<OrderSubResp> toSubRespsWithItems(List<OrderSubDO> subs) {
        if (CollUtil.isEmpty(subs)) {
            return Collections.emptyList();
        }
        List<OrderSubResp> resps = orderSubConverter.toRespList(subs);
        List<Long> subIds = subs.stream()
                                .map(OrderSubDO::getId)
                                .toList();
        List<OrderItemDO> items = orderItemMapper.selectList(
                Wrappers.<OrderItemDO>lambdaQuery().in(OrderItemDO::getSubOrderId, subIds)
        );
        Map<Long, List<OrderItemDO>> itemsBySub = items.stream()
                                                       .collect(
                                                               Collectors.groupingBy(
                                                                       OrderItemDO::getSubOrderId
                                                               )
                                                       );
        for (OrderSubResp subResp : resps) {
            List<OrderItemDO> subItems = itemsBySub.getOrDefault(subResp.getId(), Collections.emptyList());
            subResp.setItems(orderItemConverter.toItemRespList(subItems));
        }
        return resps;
    }

    /**
     * 组装单个子订单响应并补充明细。
     *
     * @param sub 子订单实体
     * @return 子订单响应
     */
    private OrderSubResp toSubRespWithItems(OrderSubDO sub) {
        List<OrderItemDO> items = orderItemMapper.selectList(
                Wrappers.<OrderItemDO>lambdaQuery().eq(OrderItemDO::getSubOrderId, sub.getId())
        );
        OrderSubResp resp = orderSubConverter.toResp(sub);
        resp.setItems(orderItemConverter.toItemRespList(items));
        return resp;
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 用户 ID
     */
    private Long requireUserId() {
        return Asserts.requireNonNull(UserContextHolder.getUserId(), BizError.USER_NOT_AUTHENTICATED);
    }

    /**
     * 校验收货地址归属并返回地址快照。
     *
     * @param userId 用户 ID
     * @param addressId 地址 ID
     * @return 收货地址
     */
    private AddressResp requireAddress(Long userId, Long addressId) {
        List<AddressResp> addresses = addressClient.listAddresses(userId);
        if (CollUtil.isEmpty(addresses)) {
            throw new BizException(BizError.ORDER_CREATE_ADDRESS_NOT_EXIST);
        }
        return addresses.stream()
                        .filter(address -> Objects.equals(address.getId(), addressId))
                        .findFirst()
                        .orElseThrow(() -> new BizException(BizError.ORDER_CREATE_ADDRESS_NOT_EXIST));
    }

    /**
     * 合并下单明细为 skuId → 数量（同 SKU 累加）。
     *
     * @param items 下单明细
     * @return skuId → 数量
     */
    private Map<Long, Integer> mergeQuantities(List<OrderItemReq> items) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderItemReq item : items) {
            quantities.merge(item.getSkuId(), item.getQuantity(), Integer::sum);
        }
        return quantities;
    }

    /**
     * 校验下单 SKU 均为购物车选中项且数量未超购物车。
     *
     * @param userId 用户 ID
     * @param quantities skuId → 数量
     * @return 购物车选中项 map
     */
    private Map<Long, CartDO> requireCartSelection(Long userId, Map<Long, Integer> quantities) {
        List<CartDO> selected = cartService.listSelectedItems(userId);
        if (CollUtil.isEmpty(selected)) {
            throw new BizException(BizError.GOODS_NOT_IN_CART);
        }
        Map<Long, CartDO> cartMap = selected.stream()
                                            .collect(Collectors.toMap(CartDO::getSkuId, Function.identity()));
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            CartDO cart = Asserts.requireNonNull(cartMap.get(entry.getKey()), BizError.GOODS_NOT_IN_CART);
            Asserts.isTrue(cart.getQuantity() >= entry.getValue(), BizError.ORDER_QUANTITY_INVALID);
        }
        return cartMap;
    }

    /**
     * 批量获取可信 SKU 快照并校验上下架与库存。
     *
     * @param quantities skuId → 数量
     * @return SKU 可信快照 map
     */
    private Map<Long, SkuResp> requireAvailableSkus(Map<Long, Integer> quantities) {
        List<SkuResp> skus = skuClient.listSkusByIds(new ArrayList<>(quantities.keySet()));
        if (CollUtil.isEmpty(skus)) {
            throw new BizException(BizError.ORDER_CREATE_SKU_NOT_AVAILABLE);
        }
        Map<Long, SkuResp> skuMap = skus.stream()
                                        .collect(Collectors.toMap(SkuResp::getSkuId, Function.identity()));
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            SkuResp sku = Asserts.requireNonNull(
                    skuMap.get(entry.getKey()),
                    BizError.ORDER_CREATE_SKU_NOT_AVAILABLE
            );
            Asserts.isTrue(sku.getStatus() == StatusEnum.ENABLED, BizError.ORDER_CREATE_SKU_NOT_AVAILABLE);
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
    private List<CouponCalcItem> buildCalcItems(Map<Long, Integer> quantities, Map<Long, SkuResp> skuMap) {
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

    /**
     * 调用优惠券服务重新计算并锁定用户券。
     *
     * @param userId 用户 ID
     * @param orderNo 订单号
     * @param req 下单请求
     * @param calcItems 可信商品明细
     * @return 优惠快照
     */
    private CouponOrderCalcResp lockCoupons(
            Long userId,
            String orderNo,
            CreateOrderReq req,
            List<CouponCalcItem> calcItems
    ) {
        CouponOrderLockReq lockReq = CouponOrderLockReq
                .builder()
                .orderNo(orderNo)
                .lockExpireTime(LocalDateTime.now().plusMinutes(LOCK_EXPIRE_MINUTES))
                .userId(userId)
                .items(calcItems)
                .userCouponIds(req.getUserCouponIds())
                .build();
        return couponOrderClient.lock(lockReq);
    }

    /**
     * Saga 补偿：先释放已锁用户券，再释放库存，最后标记订单取消。 每个动作尽力而为且幂等，失败由超时
     * 恢复任务兜底。
     *
     * @param skeleton 骨架
     * @param userId 用户 ID
     * @param orderNo 订单号
     * @param quantities skuId → 数量
     * @param stockReserved 是否已预占库存
     * @param lockedUsages 已锁定的券快照
     */
    private void compensate(
            OrderDraft skeleton,
            Long userId,
            String orderNo,
            Map<Long, Integer> quantities,
            boolean stockReserved,
            List<CouponUsageResult> lockedUsages
    ) {
        releaseCouponsQuietly(userId, orderNo, lockedUsages);
        if (stockReserved) {
            releaseStockQuietly(orderNo, quantities);
        }
        try {
            orderPersistence.markCancelled(skeleton.order().getId());
        } catch (RuntimeException e) {
            log.warn("Saga 补偿标记订单取消失败 orderNo={}", orderNo, e);
        }
    }

    /**
     * 释放已锁定的用户券（尽力而为，幂等）。
     *
     * @param userId 用户 ID
     * @param orderNo 订单号
     * @param lockedUsages 已锁定的券快照
     */
    private void releaseCouponsQuietly(Long userId, String orderNo, List<CouponUsageResult> lockedUsages) {
        if (CollUtil.isEmpty(lockedUsages)) {
            return;
        }
        for (CouponUsageResult usage : lockedUsages) {
            try {
                couponOrderClient.release(
                        CouponOrderReleaseReq.builder()
                                             .userId(userId)
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
     * @param orderNo 订单号
     * @param quantities skuId → 数量
     */
    private void releaseStockQuietly(String orderNo, Map<Long, Integer> quantities) {
        try {
            stockClient.release(buildStockOpReq(orderNo, quantities, COMPENSATE_REASON));
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
