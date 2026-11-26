package com.cloudmall.order.application.checkout.step;

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
    /** 检查是否为单商品项 */
    CHECK_SINGLE_ITEM("检查是否为单商品项"),

    /** 加载收货地址 */
    LOAD_DELIVERY_ADDRESS("加载收货地址"),

    /** 加载购物车已选中商品 */
    LOAD_SELECTED_CART_ITEMS("加载购物车已选中商品"),

    /** 加载请求中的商品项 */
    LOAD_REQUEST_ITEMS("加载请求中的商品项"),

    /** 加载可信 SKU 快照并校验库存 */
    LOAD_TRUSTED_SKU_SNAPSHOTS("加载可信 SKU 快照并校验库存"),

    /** 执行结算 Saga */
    EXECUTE_CHECKOUT_SAGA("执行结算 Saga"),

    /** 幂等创建秒杀订单（父/子/明细） */
    CREATE_SECKILL_ORDER("幂等创建秒杀订单"),

    /** 删除已下单的购物车项（尽力而为，失败不回滚订单） */
    REMOVE_ORDERED_CART_ITEMS("删除已下单的购物车项");
    /** 枚举描述 */
    private final String desc;
}
