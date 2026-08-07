package com.cloudmall.order.step;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 结算步骤枚举
 * <p>
 * 用于步骤注册与结算类型步骤编排的标识。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum CheckoutStepEnum {
    /** 单商品校验 */
    VALIDATE_SINGLE_ITEM("单商品项校验"),

    /** 收货地址 */
    LOAD_ADDRESS("收货地址加载"),

    /** 购物车结算项加载 */
    LOAD_CART_CHECKOUT_ITEMS("从购物车加载结算项"),

    /** 请求结算项加载 */
    LOAD_REQUEST_CHECKOUT_ITEMS("从客户端请求加载结算项"),

    /** SKU 加载与校验 */
    LOAD_SKUS("SKU 加载与校验"),

    /** 预订单落库 */
    PLACE_PRE_ORDER("预订单落库"),

    /** 秒杀订单落库 */
    PLACE_SECKILL_ORDER("秒杀订单落库"),

    /** 清理购物车 */
    CLEAN_CART("清理购物车");
    /** 枚举描述 */
    private final String desc;
}
