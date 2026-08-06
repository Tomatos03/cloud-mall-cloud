package com.cloudmall.order.service.impl;

import java.math.BigDecimal;
import java.util.List;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.order.api.request.SeckillOrderCreateReq;
import com.cloudmall.order.api.response.OrderResp;
import com.cloudmall.order.convert.OrderConverter;
import com.cloudmall.order.entity.OrderDO;
import com.cloudmall.order.entity.OrderItemDO;
import com.cloudmall.order.entity.OrderSubDO;
import com.cloudmall.order.enums.OrderTypeEnum;
import com.cloudmall.order.enums.ParentOrderStatusEnum;
import com.cloudmall.order.enums.SubOrderStatusEnum;
import com.cloudmall.order.mapper.OrderItemMapper;
import com.cloudmall.order.mapper.OrderMapper;
import com.cloudmall.order.mapper.OrderSubMapper;
import com.cloudmall.order.service.ISeckillOrderCreateService;
import com.cloudmall.product.api.client.SkuClient;
import com.cloudmall.product.api.response.SkuResp;
import com.cloudmall.user.api.client.AddressClient;
import com.cloudmall.user.api.response.AddressResp;

/**
 * 秒杀订单建单服务
 * <p>
 * 跳过购物车、优惠券与普通库存预占：以 SKU 可信快照 + 秒杀价直接建到 PENDING_PAYMENT。
 * 订单号由秒杀侧预生成，uk_order_no 唯一键兜底幂等，重复投递返回已有订单。
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillOrderCreateService implements ISeckillOrderCreateService {

    private final OrderMapper orderMapper;
    private final OrderSubMapper orderSubMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderConverter orderConverter;
    private final SkuClient skuClient;
    private final AddressClient addressClient;

    /**
     * 创建秒杀订单（幂等）。
     *
     * @param req 建单请求
     * @return 订单响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResp createSeckillOrder(SeckillOrderCreateReq req) {
        OrderDO existing = orderMapper.selectByOrderNo(req.getOrderNo());
        if (existing != null) {
            log.info("秒杀订单已存在，幂等返回 orderNo={}", req.getOrderNo());
            return orderConverter.toResp(existing);
        }

        SkuResp sku = loadSku(req.getSkuId());
        String addressSnapshot = loadAddressSnapshot(req.getUserId(), req.getAddressId());

        BigDecimal amount = req.getSeckillPrice().multiply(BigDecimal.valueOf(req.getQuantity()));
        OrderDO order = buildOrder(req, amount, addressSnapshot);

        try {
            orderMapper.insert(order);
        } catch (DuplicateKeyException e) {
            // 并发重复投递：唯一键冲突后回查已有订单
            log.info("秒杀订单唯一键冲突，幂等返回 orderNo={}", req.getOrderNo());
            return orderConverter.toResp(orderMapper.selectByOrderNo(req.getOrderNo()));
        }

        OrderSubDO sub = buildSubOrder(order, sku.getShopId(), amount);
        orderSubMapper.insert(sub);
        orderItemMapper.insert(buildOrderItem(req, sku, sub.getId(), amount));

        log.info("秒杀订单创建成功 orderNo={} userId={}", order.getOrderNo(), order.getUserId());
        return orderConverter.toResp(order);
    }

    /**
     * 取 SKU 可信快照并校验上架状态。
     *
     * @param skuId SKU ID
     * @return SKU 快照
     */
    private SkuResp loadSku(Long skuId) {
        List<SkuResp> skus = skuClient.listSkusByIds(List.of(skuId));
        Asserts.notEmpty(skus, BizError.ORDER_CREATE_SKU_NOT_AVAILABLE);

        SkuResp sku = skus.get(0);
        Asserts.isTrue(StatusEnum.ENABLED.equals(sku.getStatus()), BizError.ORDER_CREATE_SKU_NOT_AVAILABLE);
        return sku;
    }

    /**
     * 解析收货地址并序列化为快照。
     *
     * @param userId 用户 ID
     * @param addressId 地址 ID
     * @return 地址快照 JSON
     */
    private String loadAddressSnapshot(Long userId, Long addressId) {
        AddressResp address = addressClient.listAddresses(userId)
                                           .stream()
                                           .filter(a -> a.getId().equals(addressId))
                                           .findFirst()
                                           .orElse(null);
        Asserts.notNull(address, BizError.ORDER_CREATE_ADDRESS_NOT_EXIST);
        return JSONUtil.toJsonStr(address);
    }

    /**
     * 构建待支付父订单（秒杀单无优惠与运费）。
     *
     * @param req 建单请求
     * @param amount 应付金额
     * @param addressSnapshot 地址快照
     * @return 父订单实体
     */
    private static OrderDO buildOrder(SeckillOrderCreateReq req, BigDecimal amount, String addressSnapshot) {
        return OrderDO.builder()
                      .id(IdWorker.getId())
                      .orderNo(req.getOrderNo())
                      .userId(req.getUserId())
                      .orderType(OrderTypeEnum.SECKILL)
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
     * @param req 建单请求
     * @param sku SKU 快照
     * @param subOrderId 子订单 ID
     * @param amount 应付金额
     * @return 明细实体
     */
    private static OrderItemDO buildOrderItem(
            SeckillOrderCreateReq req,
            SkuResp sku,
            Long subOrderId,
            BigDecimal amount
    ) {
        return OrderItemDO.builder()
                          .subOrderId(subOrderId)
                          .productId(req.getProductId())
                          .skuId(req.getSkuId())
                          .shopId(sku.getShopId())
                          .categoryId(sku.getCategoryId())
                          .productName(sku.getProductName())
                          .productImage(sku.getImage())
                          .specSnapshot(sku.getSpecDesc())
                          .unitPrice(req.getSeckillPrice())
                          .quantity(req.getQuantity())
                          .originalAmount(amount)
                          .shopDiscountAmount(BigDecimal.ZERO)
                          .platformDiscountAmount(BigDecimal.ZERO)
                          .payableAmount(amount)
                          .build();
    }
}
