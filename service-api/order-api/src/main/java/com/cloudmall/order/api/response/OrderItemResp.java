package com.cloudmall.order.api.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单明细响应（OrderItemResp）
 * <p>
 * 保存商品展示快照与金额优惠分摊快照，下单后不重新读取商品当前数据。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResp {

    /** 明细 ID */
    private Long id;

    /** 所属子订单 ID */
    private Long subOrderId;

    /** 商品 SPU ID */
    private Long productId;

    /** SKU ID */
    private Long skuId;

    /** 店铺 ID */
    private Long shopId;

    /** 商品分类 ID */
    private Long categoryId;

    /** 商品名称（快照） */
    private String productName;

    /** 商品图片（快照） */
    private String productImage;

    /** 规格快照 */
    private String specSnapshot;

    /** 成交单价（快照） */
    private BigDecimal unitPrice;

    /** 购买数量 */
    private Integer quantity;

    /** 原价金额（单价乘数量） */
    private BigDecimal originalAmount;

    /** 店铺券优惠金额 */
    private BigDecimal shopDiscountAmount;

    /** 平台券优惠金额 */
    private BigDecimal platformDiscountAmount;

    /** 应付金额 */
    private BigDecimal payableAmount;
}
