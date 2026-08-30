package com.cloudmall.seckill.api.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀支付成功事件（ORDER_EVENTS / OrderPaid 的秒杀侧视图）
 * <p>
 * 与 {@code OrderEvent} 的线上报文结构一致，但独立成类型：
 * 消费模板对同一 tag 只分发给首个匹配的处理器，若与券核销处理器共用
 * {@code EventHandler<OrderEvent>} 会互相抢占；独立载荷类型使秒杀侧获得
 * 独立的处理器列表，配合独立消费组各自收到一份消息。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillPaidEvent {

    /** 父订单号 */
    private String orderNo;

    /** 下单用户 ID */
    private Long userId;
}
