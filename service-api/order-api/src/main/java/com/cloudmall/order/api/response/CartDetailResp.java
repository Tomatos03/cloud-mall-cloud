package com.cloudmall.order.api.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.order.api.model.CartShopGroup;

/**
 * 购物车摘要响应
 * <p>
 * 包含按店铺分组的商品明细及全车汇总统计（总数量、总金额、选中金额等）。
 * 前端按 groups 渲染店铺卡片，每个店铺独立控制全选/取消。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDetailResp {

    /** 按店铺分组的商品列表 */
    private List<CartShopGroup> groups;

    /** 商品种数 */
    private Integer totalItems;

    /** 商品总数量（所有商品 quantity 之和） */
    private Integer totalQuantity;

    /** 已选中商品数量 */
    private Integer selectedCount;

    /** 商品总金额（含未选中） */
    private BigDecimal totalAmount;

    /** 已选中商品金额 */
    private BigDecimal selectedAmount;
}
