package com.cloudmall.order.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 父订单状态枚举
 * <p>
 * 对应 cm_order.status 列的取值集合。父订单负责支付聚合和用户视角的完整订单，
 * 支付前只能整体取消，支付后由子订单独立履约。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum ParentOrderStatusEnum {
    /** 创建中 — 订单骨架已建，等待库存预占、锁券和金额写入 */
    CREATING("CREATING", "创建中"),

    /** 待支付 — 创建流程完成，等待用户支付 */
    PENDING_PAYMENT("PENDING_PAYMENT", "待支付"),

    /** 已支付 — 支付成功，子订单进入履约流程 */
    PAID("PAID", "已支付"),

    /** 超时关闭 — 待支付超时，终态 */
    TIMEOUT("TIMEOUT", "超时关闭"),

    /** 已取消 — 整体取消，终态 */
    CANCELLED("CANCELLED", "已取消"),

    /** 部分退款 — 至少一个子订单完成部分退款 */
    PARTIAL_REFUNDED("PARTIAL_REFUNDED", "部分退款"),

    /** 已全额退款 — 全部子订单均完成全额退款，终态 */
    REFUNDED("REFUNDED", "已全额退款");
    /** 数据库存储值 */
    @EnumValue
    private final String code;
    /** 状态描述 */
    private final String desc;
    /**
     * 根据编码查找父订单状态。
     *
     * @param code 状态编码，如 "PAID"
     * @return 匹配的父订单状态
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    public static ParentOrderStatusEnum fromCode(String code) {
        return Arrays
                .stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid parent order status: '" + code + "', valid values: "
                                        + Arrays.toString(values())
                        )
                );
    }
}
