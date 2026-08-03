package com.cloudmall.order.service;

/**
 * 订单事件服务
 * <p>
 * 处理支付成功、超时、取消、全额退款等生命周期事件（本阶段为模拟入口），
 * 状态迁移与事件 Outbox 写入在同一本地事务提交。
 *
 * @author : Tomatos
 */
public interface IOrderEventService {

    /** 支付成功事件 */
    String EVENT_PAID = "OrderPaid";

    /** 超时关闭事件 */
    String EVENT_TIMEOUT = "OrderTimeout";

    /** 取消事件 */
    String EVENT_CANCELLED = "OrderCancelled";

    /** 全额退款事件 */
    String EVENT_FULLY_REFUNDED = "OrderFullyRefunded";

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
