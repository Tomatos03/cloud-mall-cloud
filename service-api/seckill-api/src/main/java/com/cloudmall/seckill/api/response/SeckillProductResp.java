package com.cloudmall.seckill.api.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀商品响应
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillProductResp {

    /** 秒杀商品 ID */
    private Long id;

    /** 活动 ID */
    private Long activityId;

    /** 商品 SPU ID */
    private Long productId;

    /** SKU ID */
    private Long skuId;

    /** 店铺 ID */
    private Long shopId;

    /** 秒杀价 */
    private BigDecimal seckillPrice;

    /** 秒杀库存 */
    private Integer stock;

    /** 已售数量 */
    private Integer soldCount;

    /** 审核状态 */
    private String auditStatus;
}
