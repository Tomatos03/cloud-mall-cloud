package com.cloudmall.seckill.application;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import com.cloudmall.common.constant.SnPrefixConstants;
import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.api.client.InternalOrderClient;
import com.cloudmall.order.api.model.req.OrderEventReq;
import com.cloudmall.product.api.client.SkuClient;
import com.cloudmall.product.api.model.resp.SkuResp;
import com.cloudmall.seckill.api.model.req.ActivityProductSubmitReq;
import com.cloudmall.seckill.api.model.req.SeckillPurchaseReq;
import com.cloudmall.seckill.api.model.resp.PurchaseResp;
import com.cloudmall.seckill.enums.SeckillOrderStatusEnum;
import com.cloudmall.seckill.enums.SeckillPurchaseResultEnum;
import com.cloudmall.seckill.handler.SeckillBlockHandlers;
import com.cloudmall.seckill.infrastructure.cache.SeckillCacheService;
import com.cloudmall.seckill.infrastructure.ratelimit.SeckillRateLimitService;
import com.cloudmall.seckill.model.entity.SeckillActivityDO;
import com.cloudmall.seckill.model.entity.SeckillOrderDO;
import com.cloudmall.seckill.model.entity.SeckillProductDO;
import com.cloudmall.seckill.service.ISeckillActivityService;
import com.cloudmall.seckill.service.ISeckillOrderService;
import com.cloudmall.seckill.service.ISeckillProductService;

/**
 * 秒杀编排层
 * <p>
 * 商家报名：经 product-service 取 SKU 可信快照，解析 productId / shopId 并校验价格与库存，
 * 避免客户端伪造归属与原价。纯本地转发由 Controller 直接调用 Service。
 * <p>
 * 用户抢购：用户限流 → 活动窗口校验 → 商品已审校验 → Lua 原子扣减（一人一单 + 库存）
 * → 落库秒杀单 + 事务消息。立即返回受理结果，真实订单由消费者异步建立。
 * 超时回收：扫描未支付秒杀单，先状态后归还，再远程幂等取消真实订单。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillApp {

    /** 固定限购数量 */
    private static final int FIXED_QUANTITY = 1;

    private final ISeckillActivityService seckillActivityService;
    private final ISeckillProductService seckillProductService;
    private final ISeckillOrderService seckillOrderService;
    private final SkuClient skuClient;
    private final SeckillCacheService seckillCacheService;
    private final SeckillRateLimitService seckillRateLimitService;
    private final InternalOrderClient internalOrderClient;

    /**
     * 商家提交秒杀商品。
     *
     * @param userId 提交人用户 ID（由控制器从登录态传入）
     * @param shopId 当前用户店铺 ID（由控制器从登录态传入）
     * @param req 提交请求（活动 ID 由控制器从路径参数设置）
     * @return 新秒杀商品 ID
     */
    public Long submitProduct(Long userId, Long shopId, ActivityProductSubmitReq req) {
        SkuResp sku = skuClient.getById(req.getSkuId());
        checkSkuExists(sku);
        checkSubmittedShopOwnedByUser(req.getShopId(), shopId);
        checkSkuBelongsToShop(sku, shopId);
        checkValidSeckillPrice(req.getSeckillPrice(), sku.getPrice());
        checkValidSeckillStock(req.getStock(), sku.getStock());
        SeckillProductDO product = buildProduct(req, sku);
        return seckillProductService.submit(product, userId);
    }

    private void checkSkuExists(SkuResp sku) {
        Asserts.requireNonNull(sku, BizErrorEnum.PRODUCT_NOT_FOUND);
    }

    private void checkSubmittedShopOwnedByUser(Long submittedShopId, Long currentShopId) {
        Asserts.notNull(currentShopId, BizErrorEnum.MERCHANT_STORE_NOT_FOUND);
        Asserts.isEqual(submittedShopId, currentShopId, BizErrorEnum.MERCHANT_NO_SHOP);
    }

    private void checkSkuBelongsToShop(SkuResp sku, Long shopId) {
        Asserts.isEqual(sku.getShopId(), shopId, BizErrorEnum.MERCHANT_NO_SHOP);
    }

    private void checkValidSeckillPrice(BigDecimal seckillPrice, BigDecimal originalPrice) {
        Asserts.isTrue(
                seckillPrice.compareTo(originalPrice) < 0,
                BizErrorEnum.SECKILL_PRICE_MUST_LESS_THAN_ORIGINAL
        );
    }

    private void checkValidSeckillStock(Integer seckillStock, Integer skuStock) {
        Asserts.isTrue(seckillStock <= skuStock, BizErrorEnum.SECKILL_STOCK_EXCEEDS_PRODUCT_STOCK);
    }

    private static SeckillProductDO buildProduct(ActivityProductSubmitReq req, SkuResp sku) {
        return SeckillProductDO.builder()
                               .activityId(req.getActivityId())
                               .productId(sku.getProductId())
                               .skuId(sku.getSkuId())
                               .shopId(sku.getShopId())
                               .seckillPrice(req.getSeckillPrice())
                               .stock(req.getStock())
                               .build();
    }

    /**
     * 抢购（用户身份由控制器从登录态传入，不接受客户端传入）。
     *
     * @param userId 用户 ID（由控制器从登录态传入）
     * @param req 抢购请求
     * @return 受理结果（秒杀单 ID + 预生成订单号）
     */
    @SentinelResource(
            value = "seckill:purchase",
            blockHandler = "purchaseBlock",
            blockHandlerClass = SeckillBlockHandlers.class
    )
    public PurchaseResp purchase(Long userId, SeckillPurchaseReq req) {
        LocalDateTime now = LocalDateTime.now();
        checkUserRateLimit(userId);
        SeckillActivityDO activity = requireRunningActivity(req, now);
        SeckillProductDO product = requireActivityProduct(req);
        checkAndDeductCacheStock(req, activity, userId, now);
        SeckillOrderDO order = buildSeckillOrder(req, product, userId);
        createPreOrder(req, order, userId);
        return toPurchaseResp(order);
    }

    /**
     * 校验用户抢购资格（固定窗口限流）。
     */
    private void checkUserRateLimit(Long userId) {
        seckillRateLimitService.checkRate(userId);
    }

    /**
     * 校验活动存在且处于进行中窗口。
     */
    private SeckillActivityDO requireRunningActivity(SeckillPurchaseReq req, LocalDateTime now) {
        SeckillActivityDO activity = seckillActivityService.getById(req.getActivityId());
        Asserts.isTrue(activity.isRunning(), BizErrorEnum.INVALID_ACTIVITY_STATUS);
        Asserts.isTrue(activity.isStarted(now), BizErrorEnum.SECKILL_NOT_STARTED);
        Asserts.isTrue(activity.isNotEnded(now), BizErrorEnum.SECKILL_ALREADY_ENDED);
        return activity;
    }

    /**
     * 校验商品属于该活动且已审核通过。
     */
    private SeckillProductDO requireActivityProduct(SeckillPurchaseReq req) {
        return Asserts.requireNonNull(
                seckillProductService.getApprovedByActivityAndSku(req.getActivityId(), req.getSkuId()),
                BizErrorEnum.SECKILL_PRODUCT_NOT_FOUND
        );
    }

    /**
     * Lua 原子校验一人一单并预扣库存，标记 TTL 取到活动结束时刻。
     */
    private void checkAndDeductCacheStock(
        SeckillPurchaseReq req,
        SeckillActivityDO activity,
        Long userId,
        LocalDateTime now
    ) {
        long ttlSeconds = Math.max(Duration.between(now, activity.getEndTime()).getSeconds(), 1);
        SeckillPurchaseResultEnum result = seckillCacheService.purchase(
                req.getActivityId(),
                req.getSkuId(),
                userId,
                FIXED_QUANTITY,
                ttlSeconds
        );
        Asserts.notEqual(result, SeckillPurchaseResultEnum.REPEAT, BizErrorEnum.SECKILL_REPEAT_ORDER);
        Asserts.notEqual(result, SeckillPurchaseResultEnum.NO_STOCK, BizErrorEnum.SECKILL_STOCK_INSUFFICIENT);
    }

    private static SeckillOrderDO buildSeckillOrder(
        SeckillPurchaseReq req,
        SeckillProductDO product,
        Long userId
    ) {
        return SeckillOrderDO.builder()
                             .orderNo(SnPrefixConstants.SECKILL_ORDER + IdUtil.getSnowflakeNextIdStr())
                             .status(SeckillOrderStatusEnum.PENDING_PAYMENT.getCode())
                             .activityId(req.getActivityId())
                             .productId(product.getProductId())
                             .skuId(req.getSkuId())
                             .userId(userId)
                             .seckillPrice(product.getSeckillPrice())
                             .quantity(FIXED_QUANTITY)
                             .build();
    }

    /**
     * 创建预订单并落事务消息；唯一键冲突说明 Redis 标记失效，回补预扣库存后拒绝重复下单。
     */
    private void createPreOrder(SeckillPurchaseReq req, SeckillOrderDO order, Long userId) {
        try {
            seckillOrderService.createWithEvent(order, req.getAddressId());
        } catch (DuplicateKeyException e) {
            // DB 唯一键是一人一单的最终权威：Redis 标记可能因过期或重建而失效
            seckillCacheService.rollback(req.getActivityId(), req.getSkuId(), userId);
            log.warn("秒杀单唯一键冲突，已回补库存 userId={} skuId={}", userId, req.getSkuId());
            throw new BizException(BizErrorEnum.SECKILL_REPEAT_ORDER);
        }
    }

    private static PurchaseResp toPurchaseResp(SeckillOrderDO order) {
        return PurchaseResp.builder()
                           .seckillOrderId(order.getId())
                           .orderNo(order.getOrderNo())
                           .status(order.getStatus())
                           .build();
    }

    /**
     * 超时回收未支付的秒杀单。 <p>
     * 扫描 15 分钟未支付的秒杀单，按“先状态后归还”顺序处理： 条件更新置 CANCELLED → 仅
     * 当本次成功取消才归还 Redis 库存 → 幂等取消真实订单。 与支付确认的竞态由状态条件更新吃掉，
     * 避免多还库存。
     */
    public void recoverTimeoutOrders() {
        List<SeckillOrderDO> timeoutOrders;
        try {
            timeoutOrders = seckillOrderService.scanTimeoutOrders();
        } catch (Exception e) {
            log.error("扫描超时秒杀单失败", e);
            return;
        }
        if (CollectionUtil.isEmpty(timeoutOrders)) {
            return;
        }

        int recovered = 0;
        for (SeckillOrderDO order : timeoutOrders) {
            try {
                // 先状态：影响行数为 0 说明已被支付确认抢先，不得归还库存
                if (!seckillOrderService.cancelIfPending(order.getId())) {
                    continue;
                }
                // 只归还库存、保留一人一单标记：秒杀单行仍在（CANCELLED），
                // 唯一键继续拦截重复下单，清标记只会招致无谓的 Lua 预扣与回补
                seckillCacheService.refundStock(order.getActivityId(), order.getSkuId());
                cancelRealOrder(order.getOrderNo());
                recovered++;
            } catch (Exception e) {
                log.error("秒杀单超时回收失败 seckillOrderId={}", order.getId(), e);
            }
        }
        if (recovered > 0) {
            log.info("秒杀单超时回收完成 数量={}", recovered);
        }
    }

    /**
     * 幂等取消真实订单，失败不影响秒杀侧已完成的库存归还。
     *
     * @param orderNo 真实订单号
     */
    private void cancelRealOrder(String orderNo) {
        if (StrUtil.isBlank(orderNo)) {
            return;
        }
        try {
            internalOrderClient.cancel(OrderEventReq.builder()
                                                    .orderNo(orderNo)
                                                    .build());
        } catch (Exception e) {
            log.warn("取消真实订单失败，等待下轮补偿 orderNo={}", orderNo, e);
        }
    }
}
