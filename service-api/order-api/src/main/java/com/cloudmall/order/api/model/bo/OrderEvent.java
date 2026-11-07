package com.cloudmall.order.api.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单事件
 * <p>
 * order-service 状态变更通过 RocketMQ 事务消息按 tag 发布：
 * OrderPaid / OrderTimeout / OrderCancelled / OrderFullyRefunded。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    /** 父订单号 */
    private String orderNo;

    /** 下单用户 ID */
    private Long userId;

    /** 退款单 ID（全额退款事件） */
    private String refundId;
}
