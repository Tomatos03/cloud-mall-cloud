package com.cloudmall.order.api.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 购物车商品行
 * <p>
 * 单个购物车条目，携带商品快照及当前状态。
 * shopId 冗余存储，便于前端 flatMap 获得平铺视图。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    /** 所属店铺 ID */
    private Long shopId;

    /** 商品 ID */
    private Long productId;

    /** SKU ID */
    private Long skuId;

    /** 商品名称 */
    private String productName;

    /** 商品主图 URL */
    private String productImage;

    /** 规格描述，例如 "红色；XL" */
    private String specDesc;

    /** 当前单价 */
    private BigDecimal price;

    /** 数量 */
    private Integer quantity;

    /** 是否选中（用于结算） */
    private Boolean selected;

    /** 商品是否可售（下架、库存不足、超限购等时为 false） */
    private Boolean available;

    /** 不可售原因，available == false 时填充，如 "库存不足"、"商品已下架" */
    private String unavailableReason;

    /** 小计金额（price × quantity） */
    private BigDecimal subtotal;
}
