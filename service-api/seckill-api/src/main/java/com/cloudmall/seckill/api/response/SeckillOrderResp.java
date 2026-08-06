package com.cloudmall.seckill.api.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀订单响应
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderResp {

    /** 秒杀单 ID */
    private Long id;

    /** 活动 ID */
    private Long activityId;

    /** 商品 SPU ID */
    private Long productId;

    /** SKU ID */
    private Long skuId;

    /** 购买用户 ID */
    private Long userId;

    /** 购买数量 */
    private Integer quantity;

    /** 秒杀价 */
    private BigDecimal seckillPrice;

    /** 真实订单号 */
    private String orderNo;

    /** 状态 */
    private String status;

    /** 创建时间 */
    private LocalDateTime createTime;
}
