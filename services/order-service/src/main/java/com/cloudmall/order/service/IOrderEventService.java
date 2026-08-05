package com.cloudmall.order.service;

/**
 * 订单事件服务
 * <p>
 * 处理支付成功、超时、取消、全额退款等生命周期事件（本阶段为模拟入口），
 * 状态迁移与事件 Outbox 写入在同一本地事务提交。
 * <p>
 * 事件类型复用 {@link com.cloudmall.order.api.event.OrderTagEnum} 作为 RocketMQ Tag。
 *
 * @author : Tomatos
 */
public interface IOrderEventService {

    /**
     * 支付成功：PENDING_PAYMENT → PAID。
     *
     * @param orderNo 父订单号
     */
    void handlePaid(String orderNo);

    /**
     * 支付超时：PENDING_PAYMENT → TIMEOUT。
     *
     * @param orderNo 父订单号
     */
    void handleTimeout(String orderNo);

    /**
     * 订单取消：待支付父订单整体取消。
     *
     * @param orderNo 父订单号
     */
    void handleCancel(String orderNo);

    /**
     * 全额退款：PAID/PARTIAL_REFUNDED → REFUNDED。
     *
     * @param orderNo 父订单号
     * @param refundId 退款单 ID
     */
    void handleFullyRefunded(String orderNo, String refundId);
}
