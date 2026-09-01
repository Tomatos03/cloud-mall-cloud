package com.cloudmall.seckill.application;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.api.client.InternalOrderClient;
import com.cloudmall.order.api.request.PlainSeckillOrderReq;
import com.cloudmall.product.api.client.SkuClient;
import com.cloudmall.product.api.response.SkuResp;
import com.cloudmall.seckill.api.request.SeckillPurchaseReq;
import com.cloudmall.seckill.api.response.PurchaseResp;
import com.cloudmall.seckill.entity.SeckillActivityDO;
import com.cloudmall.seckill.entity.SeckillProductDO;
import com.cloudmall.seckill.enums.SeckillOrderStatusEnum;
import com.cloudmall.seckill.mapper.SeckillProductMapper;
import com.cloudmall.seckill.service.ISeckillActivityService;
import com.cloudmall.seckill.service.ISeckillProductService;
import com.cloudmall.user.api.client.InternalUserClient;
import com.cloudmall.user.api.response.AddressResp;

/**
 * 普通流程秒杀抢购编排
 * <p>
 * 普通方案（同步 DB 建单）对比实验入口：不走 Redis 预扣与事务消息，同一请求内
 * 完成「活动/商品校验 → DB 条件扣减秒杀池 → 同步 Feign 建真实订单」。
 * <ul>
 *   <li>防超卖：cm_seckill_product 条件 UPDATE（stock &gt;= 1），单语句自提交
 *       不跨远程调用持有行锁</li>
 *   <li>一人一单：Redis SETNX 标记（TTL=活动结束，弱一致）+ 订单号幂等</li>
 *   <li>失败回补：建单异常时回补已扣库存，避免压测库存被失败请求耗尽</li>
 * </ul>
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillPlainOrderApp {

    /** 固定限购数量 */
    private static final int FIXED_QUANTITY = 1;

    /** 一人一单标记 key 前缀（普通流程，与异步方案的 seckill:user: 区分） */
    private static final String PLAIN_USER_FLAG_PREFIX = "seckill:plain:user:";

    private final ISeckillActivityService activityService;
    private final ISeckillProductService productService;
    private final SeckillProductMapper productMapper;
    private final SkuClient skuClient;
    private final InternalUserClient internalUserClient;
    private final InternalOrderClient internalOrderClient;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 普通流程抢购（同步建单，非原子：扣库存先提交，建单失败回补）。
     *
     * @param req 抢购请求
     * @return 受理结果（真实订单号）
     */
    public PurchaseResp purchase(SeckillPurchaseReq req) {
        Long userId = UserContextHolder.getUserId();
        Asserts.notNull(userId, BizError.USER_NOT_AUTHENTICATED);
        Asserts.notNull(req.getAddressId(), BizError.SECKILL_ADDRESS_REQUIRED);

        SeckillActivityDO activity = activityService.getById(req.getActivityId());
        LocalDateTime now = LocalDateTime.now();
        Asserts.isTrue(activity.isRunning(), BizError.INVALID_ACTIVITY_STATUS);
        Asserts.isTrue(activity.isStarted(now), BizError.SECKILL_NOT_STARTED);
        Asserts.isTrue(activity.isNotEnded(now), BizError.SECKILL_ALREADY_ENDED);

        SeckillProductDO product = Asserts.requireNonNull(
                productService.getApprovedByActivityAndSku(req.getActivityId(), req.getSkuId()),
                BizError.SECKILL_PRODUCT_NOT_FOUND
        );

        // 一人一单第一道防重（SETNX，TTL 取到活动结束）
        long ttlSeconds = Math.max(Duration.between(now, activity.getEndTime()).getSeconds(), 1);
        String userFlagKey = PLAIN_USER_FLAG_PREFIX + req.getActivityId() + ":" + req.getSkuId() + ":"
                                     + userId;
        Boolean firstTime = stringRedisTemplate.opsForValue()
                                               .setIfAbsent(userFlagKey, "1", Duration.ofSeconds(ttlSeconds));
        Asserts.isTrue(Boolean.TRUE.equals(firstTime), BizError.SECKILL_REPEAT_ORDER);

        // DB 条件扣减秒杀池库存，0 行表示库存不足（库存以 DB 为准）
        int deducted = productMapper.deductStock(req.getActivityId(), req.getSkuId());
        Asserts.isTrue(deducted > 0, BizError.SECKILL_STOCK_INSUFFICIENT);

        String orderNo = "SK" + IdUtil.getSnowflakeNextIdStr();
        try {
            internalOrderClient.createPlainSeckillOrder(buildOrderReq(req, product, userId, orderNo));
        } catch (RuntimeException e) {
            // 建单失败：回补库存并清除一人一单标记，避免压测库存被失败请求耗尽
            productMapper.releaseStock(req.getActivityId(), req.getSkuId());
            stringRedisTemplate.delete(userFlagKey);
            log.warn("普通秒杀同步建单失败，已回补库存 userId={} skuId={}", userId, req.getSkuId(), e);
            throw e;
        }

        return PurchaseResp.builder()
                           .seckillOrderId(null)
                           .orderNo(orderNo)
                           .status(SeckillOrderStatusEnum.PENDING_PAYMENT.getCode())
                           .build();
    }

    /**
     * 组装同步建单请求（快照来自活动商品 + SKU 可信数据 + 收货地址）。
     *
     * @param req 抢购请求
     * @param product 秒杀商品
     * @param userId 用户 ID
     * @param orderNo 订单号
     * @return 建单请求
     */
    private PlainSeckillOrderReq buildOrderReq(
        SeckillPurchaseReq req,
        SeckillProductDO product,
        Long userId,
        String orderNo
    ) {
        SkuResp sku = requireSku(req.getSkuId());
        AddressResp address = requireAddress(req.getAddressId(), userId);
        return PlainSeckillOrderReq.builder()
                                   .orderNo(orderNo)
                                   .userId(userId)
                                   .productId(product.getProductId())
                                   .skuId(product.getSkuId())
                                   .shopId(product.getShopId())
                                   .categoryId(sku.getCategoryId())
                                   .productName(sku.getProductName())
                                   .productImage(sku.getImage())
                                   .specSnapshot(sku.getSpecDesc())
                                   .seckillPrice(product.getSeckillPrice())
                                   .quantity(FIXED_QUANTITY)
                                   .addressSnapshot(JSONUtil.toJsonStr(address))
                                   .build();
    }

    /**
     * 查询 SKU 可信快照（不存在时抛商品不存在）。
     *
     * @param skuId SKU ID
     * @return SKU 快照
     */
    private SkuResp requireSku(Long skuId) {
        return Asserts.requireNonNull(
                skuClient.listSkusByIds(List.of(skuId))
                         .stream()
                         .findFirst()
                         .orElse(null),
                BizError.SECKILL_PRODUCT_NOT_FOUND
        );
    }

    /**
     * 查询收货地址（不存在或不属于该用户时抛下单地址不存在）。
     *
     * @param addressId 地址 ID
     * @param userId 用户 ID
     * @return 地址快照
     */
    private AddressResp requireAddress(Long addressId, Long userId) {
        return Asserts.requireNonNull(
                internalUserClient.getAddressById(addressId, userId),
                BizError.ORDER_CREATE_ADDRESS_NOT_EXIST
        );
    }
}
