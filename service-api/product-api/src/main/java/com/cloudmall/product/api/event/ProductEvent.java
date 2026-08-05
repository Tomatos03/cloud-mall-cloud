package com.cloudmall.product.api.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品事件
 * <p>
 * product-service 商品变更写入本地 Outbox，由 RocketMQ 按 tag 发布：
 * ProductCreated / ProductUpdated / ProductDeleted。
 * eventId 为 Outbox event_id，跨服务消费幂等键。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEvent {

    /** 事件 ID（Outbox event_id，消费幂等键） */
    private String eventId;

    /** 商品 ID */
    private Long productId;

    /** SPU 编码 */
    private String spuCode;
}
