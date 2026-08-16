package com.cloudmall.seckill.application;

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

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.api.client.InternalOrderClient;
import com.cloudmall.order.api.request.OrderEventReq;
import com.cloudmall.seckill.api.request.SeckillPurchaseReq;
import com.cloudmall.seckill.api.response.PurchaseResp;
import com.cloudmall.seckill.cache.SeckillCacheService;
import com.cloudmall.seckill.cache.SeckillRateLimitService;
import com.cloudmall.seckill.entity.SeckillActivityDO;
import com.cloudmall.seckill.entity.SeckillOrderDO;
import com.cloudmall.seckill.entity.SeckillProductDO;
import com.cloudmall.seckill.enums.SeckillOrderStatusEnum;
import com.cloudmall.seckill.enums.SeckillPurchaseResultEnum;
import com.cloudmall.seckill.handler.SeckillBlockHandlers;
import com.cloudmall.seckill.service.ISeckillActivityService;
import com.cloudmall.seckill.service.ISeckillOrderService;
import com.cloudmall.seckill.service.ISeckillProductService;

/**
 * 秒杀单编排层
 * <p>
 * 抢购：黑名单与限流 → 活动窗口校验 → 商品已审校验 → Lua 原子扣减（一人一单 + 库存）
 * → 落库秒杀单 + Outbox 事件。立即返回受理结果，真实订单由消费者异步建立。
 * 超时回收：扫描未支付秒杀单，先状态后归还，再远程幂等取消真实订单。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderApp {

    /** 固定限购数量 */
    private static final int FIXED_QUANTITY = 1;

    private final ISeckillActivityService activityService;
    private final ISeckillProductService productService;
    private final ISeckillOrderService orderService;
    private final SeckillCacheService cacheService;
    private final SeckillRateLimitService rateLimitService;
    private final InternalOrderClient internalOrderClient;

    /**
     * 抢购（用户身份取自登录态，不接受客户端传入）。
     *
     * @param req 抢购请求
     * @return 受理结果（秒杀单 ID + 预生成订单号）
     */
    @SentinelResource(
            value = "seckill:purchase",
            blockHandler = "purchaseBlock",
            blockHandlerClass = SeckillBlockHandlers.class
    )
    public PurchaseResp purchase(SeckillPurchaseReq req) {
        Long userId = UserContextHolder.getUserId();
        Asserts.notNull(userId, BizError.USER_NOT_AUTHENTICATED);
        Asserts.notNull(req.getAddressId(), BizError.SECKILL_ADDRESS_REQUIRED);

        // 黑名单拦截 + 固定窗口限流
        rateLimitService.checkRate(userId);

        // 活动必须存在且处于进行中窗口
        SeckillActivityDO activity = activityService.getById(req.getActivityId());
        LocalDateTime now = LocalDateTime.now();
        Asserts.isTrue(activity.isRunning(), BizError.INVALID_ACTIVITY_STATUS);
        Asserts.isTrue(activity.isStarted(now), BizError.SECKILL_NOT_STARTED);
        Asserts.isTrue(activity.isNotEnded(now), BizError.SECKILL_ALREADY_ENDED);

        // 商品必须属于该活动且已审核通过
        SeckillProductDO product = Asserts.requireNonNull(
                productService.getApprovedByActivityAndSku(req.getActivityId(), req.getSkuId()),
                BizError.SECKILL_PRODUCT_NOT_FOUND
        );

        // 一人一单标记 TTL 取到活动结束时刻
        long ttlSeconds = Math.max(Duration.between(now, activity.getEndTime()).getSeconds(), 1);
        SeckillPurchaseResultEnum result = cacheService.purchase(
                req.getActivityId(),
                req.getSkuId(),
                userId,
                FIXED_QUANTITY,
                ttlSeconds
        );
        Asserts.notEqual(result, SeckillPurchaseResultEnum.REPEAT, BizError.SECKILL_REPEAT_ORDER);
        Asserts.notEqual(result, SeckillPurchaseResultEnum.NO_STOCK, BizError.SECKILL_STOCK_INSUFFICIENT);

        SeckillOrderDO order = buildSeckillOrder(req, product, userId);

        try {
            orderService.createWithEvent(order, req.getAddressId());
        } catch (DuplicateKeyException e) {
            // DB 唯一键是一人一单的最终权威：Redis 标记可能因过期或重建而失效
            cacheService.rollback(req.getActivityId(), req.getSkuId(), userId);
            log.warn("秒杀单唯一键冲突，已回补库存 userId={} skuId={}", userId, req.getSkuId());
            throw new BizException(BizError.SECKILL_REPEAT_ORDER);
        }

        return PurchaseResp.builder()
                           .seckillOrderId(order.getId())
                           .orderNo(order.getOrderNo())
                           .status(order.getStatus())
                           .build();
    }

    private static SeckillOrderDO buildSeckillOrder(
            SeckillPurchaseReq req,
            SeckillProductDO product,
            Long userId
    ) {
        return SeckillOrderDO.builder()
                             .orderNo("SK" + IdUtil.getSnowflakeNextIdStr())
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
     * 超时回收未支付的秒杀单。 <p>
     * 扫描 15 分钟未支付的秒杀单，按“先状态后归还”顺序处理： 条件更新置 CANCELLED → 仅
     * 当本次成功取消才归还 Redis 库存 → 幂等取消真实订单。 与支付确认的竞态由状态条件更新吃掉，
     * 避免多还库存。
     */
    public void recoverTimeoutOrders() {
        List<SeckillOrderDO> timeoutOrders;
        try {
            timeoutOrders = orderService.scanTimeoutOrders();
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
                if (!orderService.cancelIfPending(order.getId())) {
                    continue;
                }
                // 只归还库存、保留一人一单标记：秒杀单行仍在（CANCELLED），
                // 唯一键继续拦截重复下单，清标记只会招致无谓的 Lua 预扣与回补
                cacheService.refundStock(order.getActivityId(), order.getSkuId());
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
