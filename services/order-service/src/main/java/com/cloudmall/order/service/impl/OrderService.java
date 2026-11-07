package com.cloudmall.order.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.coupon.api.model.resp.CouponItemResult;
import com.cloudmall.coupon.api.model.resp.CouponOrderCalcResp;
import com.cloudmall.coupon.api.model.resp.CouponSubOrderResult;
import com.cloudmall.coupon.api.model.resp.CouponUsageResult;
import com.cloudmall.coupon.api.enums.CouponOwnerEnum;
import com.cloudmall.order.api.model.resp.CouponUsageStatsResp;
import com.cloudmall.order.api.model.resp.OrderResp;
import com.cloudmall.order.api.model.resp.OrderSubResp;
import com.cloudmall.order.convert.OrderConverter;
import com.cloudmall.order.convert.OrderCouponConverter;
import com.cloudmall.order.convert.OrderItemConverter;
import com.cloudmall.order.convert.OrderSubConverter;
import com.cloudmall.order.model.entity.OrderCouponDO;
import com.cloudmall.order.model.entity.OrderDO;
import com.cloudmall.order.model.entity.OrderItemDO;
import com.cloudmall.order.model.entity.OrderSubDO;
import com.cloudmall.order.enums.ParentOrderStatusEnum;
import com.cloudmall.order.enums.SubOrderStatusEnum;
import com.cloudmall.order.mapper.OrderCouponMapper;
import com.cloudmall.order.mapper.OrderItemMapper;
import com.cloudmall.order.mapper.OrderMapper;
import com.cloudmall.order.mapper.OrderSubMapper;
import com.cloudmall.order.model.bo.CheckoutContext;
import com.cloudmall.order.model.req.OrderPageReq;
import com.cloudmall.order.model.req.OrderSubPageReq;
import com.cloudmall.order.service.IOrderService;
import com.cloudmall.product.api.model.resp.SkuResp;

/**
 * 订单服务实现
 * <p>
 * 本地查询与响应组装（父/子订单分页、详情、券使用统计），以及下单 Saga 中需要独立事务提交的写库步骤：
 * CREATING 骨架、金额/明细/优惠落库、失败标记取消。 骨架必须先于远程调用提交，宕机时才能被
 * 超时恢复任务发现并补偿。 涉及远程调用的下单编排见
 * {@link com.cloudmall.order.application.OrderApp}。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final OrderMapper orderMapper;
    private final OrderSubMapper orderSubMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderCouponMapper orderCouponMapper;
    private final OrderConverter orderConverter;
    private final OrderSubConverter orderSubConverter;
    private final OrderItemConverter orderItemConverter;
    private final OrderCouponConverter orderCouponConverter;

    /**
     * 获取父订单详情（仅当前用户本人可查）。
     *
     * @param id 父订单 ID
     * @return 父订单详情
     */
    @Override
    public OrderResp getById(Long id) {
        OrderDO order = Asserts.requireNonNull(orderMapper.selectById(id), BizErrorEnum.ORDER_NOT_EXIST);
        Long userId = UserContextHolder.getUserId();
        Asserts.isTrue(Objects.equals(order.getUserId(), userId), BizErrorEnum.ORDER_NOT_BELONG_TO_USER);
        return toResp(order);
    }

    /**
     * 分页查询父订单（条件由调用方组装：用户端点已注入 userId，运营端点可传全量条件）。
     *
     * @param req 分页请求
     * @return 父订单分页
     */
    @Override
    public IPage<OrderResp> page(OrderPageReq req) {
        IPage<OrderDO> page = orderMapper.selectOrderPage(req);
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
        Long shopId = Asserts.requireNonNull(UserContextHolder.getShopId(), BizErrorEnum.MERCHANT_NO_SHOP);
        req.setShopId(shopId);
        IPage<OrderSubDO> page = orderSubMapper.selectMerchantPage(req);
        return page.convert(this::toSubRespWithItems);
    }

    /**
     * 组装父订单响应：子订单、明细与券快照。
     *
     * @param order 父订单实体
     * @return 父订单响应
     */
    private OrderResp toResp(OrderDO order) {
        OrderResp resp = orderConverter.toResp(order);
        List<OrderSubDO> subs = orderSubMapper.selectByParentOrderId(order.getId());
        resp.setSubs(toSubRespsWithItems(subs));
        List<OrderCouponDO> coupons = orderCouponMapper.selectByParentOrderId(order.getId());
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
        List<OrderItemDO> items = orderItemMapper.selectBySubOrderIds(subIds);
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
        List<OrderItemDO> items = orderItemMapper.selectBySubOrderId(sub.getId());
        OrderSubResp resp = orderSubConverter.toResp(sub);
        resp.setItems(orderItemConverter.toItemRespList(items));
        return resp;
    }

    /**
     * 创建预订单：仅落库 CREATING 父订单。 <p>
     * 订单号在内部生成，父订单 ID 预生成雪花值；店铺子订单在 finalizeOrder 阶段按优惠结
     * 果构建并插入。
     *
     * @param context 订单上下文
     * @return CREATING 父订单
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderDO createPreOrder(CheckoutContext context) {
        String orderNo = genOrderNo();
        OrderDO order = buildCreatingOrder(context, orderNo);
        order.setId(IdWorker.getId());
        orderMapper.insert(order);
        return order;
    }

    /**
     * 生成订单号（UUID 截断为 20 位）。
     *
     * @return 订单号
     */
    private static String genOrderNo() {
        return StrUtil.sub(IdUtil.fastSimpleUUID(), 0, 20);
    }

    /**
     * 按优惠结果构建待发货子订单（直接携带最终金额）。
     *
     * @param order 父订单
     * @param index 店铺序号（用于子订单号后缀）
     * @param subResult 子订单优惠结果
     * @param remark 店铺维度订单备注（可空）
     * @return 子订单实体
     */
    private static OrderSubDO buildSubOrder(
        OrderDO order,
        int index,
        CouponSubOrderResult subResult,
        String remark
    ) {
        return OrderSubDO.builder()
                         .id(IdWorker.getId())
                         .subOrderNo(order.getOrderNo() + "-" + (index + 1))
                         .parentOrderId(order.getId())
                         .shopId(subResult.getShopId())
                         .remark(remark)
                         .status(SubOrderStatusEnum.PENDING_SHIPMENT)
                         .originalAmount(subResult.getOriginalAmount())
                         .shopDiscountAmount(subResult.getShopDiscountAmount())
                         .platformDiscountAmount(subResult.getPlatformDiscountAmount())
                         .payableAmount(subResult.getPayableAmount())
                         .merchantSettlementAmount(subResult.getPayableAmount())
                         .build();
    }

    /**
     * 构建 CREATING 状态的父订单骨架。
     *
     * @param context 订单上下文
     * @param orderNo 订单号
     * @return 父订单实体
     */
    private static OrderDO buildCreatingOrder(CheckoutContext context, String orderNo) {
        return OrderDO.builder()
                      .orderNo(orderNo)
                      .userId(context.getUserId())
                      .orderType(context.getCheckoutType())
                      .status(ParentOrderStatusEnum.CREATING)
                      .totalOriginalAmount(BigDecimal.ZERO)
                      .totalShopDiscountAmount(BigDecimal.ZERO)
                      .totalPlatformDiscountAmount(BigDecimal.ZERO)
                      .productPayableAmount(BigDecimal.ZERO)
                      .freightAmount(BigDecimal.ZERO)
                      .payableAmount(BigDecimal.ZERO)
                      .addressSnapshot(JSONUtil.toJsonStr(context.getAddress()))
                      .remark(context.getReq().getRemark())
                      .build();
    }

    /**
     * 落库订单金额，构建并插入子订单与明细优惠分摊、券快照，并将父订单迁移为待支付。
     *
     * @param order 父订单
     * @param context 订单上下文
     * @param calc 优惠计算/锁定结果
     */
    @Transactional(rollbackFor = Exception.class)
    public void finalizeOrder(OrderDO order, CheckoutContext context, CouponOrderCalcResp calc) {
        updateParentAmounts(order.getId(), calc);
        Map<Long, OrderSubDO> subsByShop = createSubOrders(order, calc, context);
        insertItemsForSubs(subsByShop, calc, context);
        insertOrderCoupons(order, subsByShop, calc.getUsages());
        transitionToPendingPayment(order.getId());
    }

    /**
     * 按优惠结果构建并批量插入子订单（一次落库最终金额）。
     *
     * @param order 父订单
     * @param calc 优惠计算/锁定结果
     * @param context 订单上下文（店铺维度备注来源）
     * @return shopId → 子订单
     */
    private Map<Long, OrderSubDO> createSubOrders(
        OrderDO order,
        CouponOrderCalcResp calc,
        CheckoutContext context
    ) {
        List<CouponSubOrderResult> subResults = calc.getSubOrders();
        if (CollUtil.isEmpty(subResults)) {
            return Collections.emptyMap();
        }
        List<OrderSubDO> subs = new ArrayList<>(subResults.size());
        Map<Long, OrderSubDO> subsByShop = new LinkedHashMap<>();
        Map<Long, String> shopRemarks = context.getReq().getShopRemarks();
        for (int i = 0; i < subResults.size(); i++) {
            Long shopId = subResults.get(i).getShopId();
            String remark = shopRemarks == null ? null : shopRemarks.get(shopId);
            OrderSubDO sub = buildSubOrder(order, i, subResults.get(i), remark);
            subs.add(sub);
            subsByShop.put(sub.getShopId(), sub);
        }
        orderSubMapper.insert(subs);
        return subsByShop;
    }

    /**
     * 写入各子订单的商品明细。
     *
     * @param subsByShop shopId → 子订单
     * @param calc 优惠计算/锁定结果
     * @param context 订单上下文
     */
    private void insertItemsForSubs(
        Map<Long, OrderSubDO> subsByShop,
        CouponOrderCalcResp calc,
        CheckoutContext context
    ) {
        for (CouponSubOrderResult subResult : calc.getSubOrders()) {
            OrderSubDO sub = Asserts.requireNonNull(subsByShop.get(subResult.getShopId()));
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
        orderMapper.markCancelled(orderId, ParentOrderStatusEnum.CANCELLED);
    }

    /**
     * 更新父订单金额。
     *
     * @param orderId 父订单 ID
     * @param calc 优惠计算/锁定结果
     */
    private void updateParentAmounts(Long orderId, CouponOrderCalcResp calc) {
        orderMapper.updateParentAmounts(orderId, calc);
    }

    /**
     * 写入子订单明细（商品快照 + 优惠分摊快照）。
     *
     * @param sub 子订单
     * @param subResult 子订单优惠结果
     * @param context 订单上下文
     */
    private void insertItems(OrderSubDO sub, CouponSubOrderResult subResult, CheckoutContext context) {
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
                               .quantity(context.getCheckoutItems().getOrDefault(itemResult.getSkuId(), 1))
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
     * @param order 父订单
     * @param subsByShop shopId → 子订单
     * @param usages 实际使用的券快照
     */
    private void insertOrderCoupons(
        OrderDO order,
        Map<Long, OrderSubDO> subsByShop,
        List<CouponUsageResult> usages
    ) {
        if (CollUtil.isEmpty(usages)) {
            return;
        }
        List<OrderCouponDO> couponDOs = new ArrayList<>();
        for (CouponUsageResult usage : usages) {
            OrderSubDO orderSubDO = subsByShop.get(usage.getShopId());
            if (usage.getOwnerType() == CouponOwnerEnum.SHOP) {
                Asserts.notNull(orderSubDO);
            }
            OrderCouponDO orderCouponDO = orderCouponConverter.toCouponDO(
                    usage,
                    order.getId(),
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
        int result = orderMapper.transitionToPendingPayment(orderId, ParentOrderStatusEnum.PENDING_PAYMENT);
        Asserts.isTrue(result > 0, BizErrorEnum.INVALID_ORDER_STATUS);
    }

    /**
     * 查询指定模板的使用订单数与补贴金额。
     *
     * @param couponId 模板 ID
     * @return 使用统计
     */
    @Override
    public CouponUsageStatsResp usageByCoupon(Long couponId) {
        return toResp(orderCouponMapper.aggregateByCoupon(couponId));
    }

    /**
     * 查询全局使用订单数与补贴金额。
     *
     * @return 使用统计
     */
    @Override
    public CouponUsageStatsResp usageOverview() {
        return toResp(orderCouponMapper.aggregateOverview());
    }

    /**
     * 聚合结果转统计响应。
     *
     * @param row 聚合行
     * @return 统计响应
     */
    private CouponUsageStatsResp toResp(Map<String, Object> row) {
        if (row == null) {
            return CouponUsageStatsResp.builder()
                                       .orderCount(0L)
                                       .subsidyAmount(BigDecimal.ZERO)
                                       .build();
        }
        Object orderCount = row.get("order_count");
        Object subsidy = row.get("subsidy_amount");
        return CouponUsageStatsResp
                .builder()
                .orderCount(orderCount == null ? 0L : ((Number) orderCount).longValue())
                .subsidyAmount(subsidy == null ? BigDecimal.ZERO : new BigDecimal(subsidy.toString()))
                .build();
    }
}
