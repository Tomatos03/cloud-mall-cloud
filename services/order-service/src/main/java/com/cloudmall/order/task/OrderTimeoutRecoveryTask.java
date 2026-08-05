package com.cloudmall.order.task;

import java.time.LocalDateTime;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cloudmall.coupon.api.client.CouponOrderClient;
import com.cloudmall.coupon.api.request.CouponOrderReleaseByOrderReq;
import com.cloudmall.order.entity.OrderDO;
import com.cloudmall.order.enums.ParentOrderStatusEnum;
import com.cloudmall.order.mapper.OrderMapper;
import com.cloudmall.order.service.impl.OrderService;
import com.cloudmall.product.api.client.StockClient;
import com.cloudmall.product.api.request.StockReleaseByOrderReq;

/**
 * 创建中超时恢复任务
 * <p>
 * 扫描长时间停留在 CREATING 的父订单（服务中途宕机遗留），
 * 按逆序释放已锁用户券与预占库存并标记取消。
 * 每个动作基于订单号幂等，重复执行不产生副作用。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutRecoveryTask {

    /** 创建中超时阈值（分钟），与锁券过期时长一致 */
    private static final int CREATING_TIMEOUT_MINUTES = 30;

    private final OrderMapper orderMapper;
    private final OrderService orderService;
    private final CouponOrderClient couponOrderClient;
    private final StockClient stockClient;

    /**
     * 周期扫描并恢复创建中超时订单。
     */
    @Scheduled(fixedDelay = 60_000)
    public void recoverStuckCreatingOrders() {
        List<OrderDO> stuck = orderMapper.selectList(
                Wrappers.<OrderDO>lambdaQuery()
                        .eq(OrderDO::getStatus, ParentOrderStatusEnum.CREATING)
                        .lt(
                                OrderDO::getCreateTime,
                                LocalDateTime.now().minusMinutes(CREATING_TIMEOUT_MINUTES)
                        )
                        .orderByAsc(OrderDO::getId)
        );
        if (CollUtil.isEmpty(stuck)) {
            return;
        }
        for (OrderDO order : stuck) {
            recoverOne(order);
        }
    }

    /**
     * 恢复单个创建中超时订单：释放券 → 释放库存 → 标记取消。
     *
     * @param order 父订单
     */
    private void recoverOne(OrderDO order) {
        String orderNo = order.getOrderNo();
        try {
            couponOrderClient.releaseByOrder(
                    CouponOrderReleaseByOrderReq.builder()
                                                .userId(order.getUserId())
                                                .orderNo(orderNo)
                                                .build()
            );
        } catch (RuntimeException e) {
            log.warn("超时恢复释放用户券失败 orderNo={}", orderNo, e);
        }
        try {
            stockClient.releaseByOrder(StockReleaseByOrderReq.builder()
                                                             .orderNo(orderNo)
                                                             .build());
        } catch (RuntimeException e) {
            log.warn("超时恢复释放库存失败 orderNo={}", orderNo, e);
        }
        try {
            orderService.markCancelled(order.getId());
        } catch (RuntimeException e) {
            log.warn("超时恢复标记订单取消失败 orderNo={}", orderNo, e);
        }
    }
}
