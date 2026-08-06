package com.cloudmall.seckill.api.event;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀建单事件
 * <p>
 * 抢购成功后与 cm_seckill_order 同事务写入 Outbox，由 RocketMQ 发布到 SECKILL_ORDERS，
 * consumer-service 消费后调 order-service 内部接口建真实订单。
 * eventId 为 Outbox event_id，跨服务消费幂等键。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderCreateEvent {

    /** 事件 ID（Outbox event_id，消费幂等键） */
    private String eventId;

    /** 秒杀单 ID */
    private Long seckillOrderId;

    /** 真实订单号（秒杀侧预生成） */
    private String orderNo;

    /** 购买用户 ID */
    private Long userId;

    /** SKU ID */
    private Long skuId;

    /** 商品 SPU ID */
    private Long productId;

    /** 秒杀价 */
    private BigDecimal seckillPrice;

    /** 购买数量 */
    private Integer quantity;

    /** 收货地址 ID */
    private Long addressId;
}
