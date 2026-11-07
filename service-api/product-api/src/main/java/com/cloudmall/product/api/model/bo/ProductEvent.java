package com.cloudmall.product.api.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品事件
 * <p>
 * product-service 商品变更通过 RocketMQ 事务消息按 tag 发布：
 * ProductCreated / ProductUpdated / ProductDeleted。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEvent {

    /** 商品 ID */
    private Long productId;

    /** SPU 编码 */
    private String spuCode;
}
