package com.cloudmall.seckill.api.model.resp;

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

    /** 商品名称（来自商品服务，展示用） */
    private String productName;

    /** 商品主图 URL（来自商品服务，展示用） */
    private String image;

    /** SKU ID */
    private Long skuId;

    /** 店铺 ID */
    private Long shopId;

    /** 秒杀价 */
    private BigDecimal seckillPrice;

    /** 商品原价（SKU 市场价/划线价，展示用） */
    private BigDecimal originalPrice;

    /** 秒杀库存 */
    private Integer stock;

    /** 已售数量 */
    private Integer soldCount;

    /** 审核状态 */
    private String auditStatus;
}
