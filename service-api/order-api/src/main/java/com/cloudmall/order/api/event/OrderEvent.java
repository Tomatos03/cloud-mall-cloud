package com.cloudmall.order.api.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单事件
 * <p>
 * order-service 状态变更写入本地 Outbox，由 RocketMQ 按 tag 发布：
 * OrderPaid / OrderTimeout / OrderCancelled / OrderFullyRefunded。
 * eventId 为 Outbox event_id，跨服务消费幂等键。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    /** 事件 ID（Outbox event_id，消费幂等键） */
    private String eventId;

    /** 父订单号 */
    private String orderNo;

    /** 下单用户 ID */
    private Long userId;

    /** 退款单 ID（全额退款事件） */
    private String refundId;
}
