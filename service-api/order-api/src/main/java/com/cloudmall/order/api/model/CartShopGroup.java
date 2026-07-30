package com.cloudmall.order.api.model;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 店铺购物车分组
 * <p>
 * 一个店铺一个分组，包含该店铺下的商品明细及店铺级汇总。
 * 前端据此渲染店铺卡片头（shopName + 店铺级全选 + 店铺级金额）。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartShopGroup {

    /** 店铺 ID */
    private Long shopId;

    /** 店铺名称 */
    private String shopName;

    /** 店铺 Logo URL */
    private String shopLogo;

    /** 该店铺下的商品明细 */
    private List<CartItem> items;

    /** 该店铺商品种数 */
    private Integer itemCount;

    /** 该店铺已选中商品数量（用于判断"全选"状态：selectedCount == itemCount） */
    private Integer selectedCount;

    /** 该店铺已选中商品金额合计 */
    private BigDecimal selectedAmount;

    /** 该店铺是否全部选中（前端可直接使用，无需自己计算 selectedCount == itemCount） */
    private Boolean shopSelected;
}
