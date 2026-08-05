package com.cloudmall.order.service;

import com.cloudmall.order.api.event.OrderTagEnum;

/**
 * 订单服务 Outbox 服务
 * <p>
 * 订单状态变更与事件写入同一本地事务提交，由定时任务发布到 RocketMQ。
 *
 * @author : Tomatos
 */
public interface IOrderOutboxService {

    /**
     * 追加订单事件（与状态变更同事务）。
     *
     * @param orderNo 父订单号
     * @param userId 用户 ID
     * @param eventType 事件类型
     * @param refundId 退款单 ID（退款事件）
     */
    void appendOrderEvent(String orderNo, Long userId, OrderTagEnum eventType, String refundId);

    /**
     * 发布一批待发布事件，更新发布状态。
     *
     * @param batchSize 批次大小
     * @return 本次尝试发布条数
     */
    int publishPending(int batchSize);
}
