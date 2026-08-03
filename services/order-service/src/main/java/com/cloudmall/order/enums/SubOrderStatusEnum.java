package com.cloudmall.order.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 店铺子订单状态枚举
 * <p>
 * 对应 cm_order_sub.status 列的取值集合。子订单负责履约，
 * 每个父订单每个店铺最多一条子订单。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum SubOrderStatusEnum {
    /** 待发货 — 已支付或随父订单创建，等待商家发货 */
    PENDING_SHIPMENT("PENDING_SHIPMENT", "待发货"),

    /** 已发货 — 商家已发货 */
    SHIPPED("SHIPPED", "已发货"),

    /** 已收货 — 用户确认收货 */
    DELIVERED("DELIVERED", "已收货"),

    /** 已完成 — 订单完成 */
    COMPLETED("COMPLETED", "已完成"),

    /** 已取消 — 支付前父订单整体取消，终态 */
    CANCELLED("CANCELLED", "已取消"),

    /** 已退款 — 子订单全额退款，终态 */
    REFUNDED("REFUNDED", "已退款");
    /** 数据库存储值 */
    @EnumValue
    private final String code;
    /** 状态描述 */
    private final String desc;
    /**
     * 根据编码查找子订单状态。
     *
     * @param code 状态编码，如 "SHIPPED"
     * @return 匹配的子订单状态
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    public static SubOrderStatusEnum fromCode(String code) {
        return Arrays
                .stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid sub order status: '" + code + "', valid values: "
                                        + Arrays.toString(values())
                        )
                );
    }
}
