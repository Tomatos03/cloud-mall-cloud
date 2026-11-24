package com.cloudmall.common.constant;

/**
 * 业务序列号前缀常量。
 * <p>
 * 生成侧统一拼接这些前缀生成 spuCode / skuCode / orderNo，消费侧也按同一组前缀识别单据类型，
 * 避免前缀字面量散落在多个服务中。
 *
 * @author : Tomatos
 */
public interface SnPrefixConstants {

    /** 普通订单号前缀 */
    String NORMAL_ORDER = "ORD";

    /** 秒杀订单号前缀 */
    String SECKILL_ORDER = "SK";

    /** 商品 SPU 编码前缀 */
    String PRODUCT_SPU = "SPU";

    /** 商品 SKU 编码前缀 */
    String PRODUCT_SKU = "SKU";
}
