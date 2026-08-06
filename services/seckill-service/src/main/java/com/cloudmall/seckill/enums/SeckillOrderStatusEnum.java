package com.cloudmall.seckill.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 秒杀订单状态枚举
 * <p>
 * 对应 cm_seckill_order.status 列。抢购受理即 PENDING_PAYMENT，
 * 支付成功事件驱动到 PAID，超时任务驱动到 CANCELLED。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum SeckillOrderStatusEnum {
    /** 待支付 — 抢购受理成功 */
    PENDING_PAYMENT("PENDING_PAYMENT", "待支付"),

    /** 已支付 — 支付确认后落库扣减秒杀池 */
    PAID("PAID", "已支付"),

    /** 已取消 — 超时未支付，库存已归还 */
    CANCELLED("CANCELLED", "已取消"),

    /** 已退款 — 预留，全额退款事件回退时使用 */
    REFUNDED("REFUNDED", "已退款");
    /** 数据库存储值 */
    @EnumValue
    private final String code;
    /** 状态描述 */
    private final String desc;
    /**
     * 根据编码查找秒杀订单状态。
     *
     * @param code 状态编码，如 "PAID"
     * @return 匹配的秒杀订单状态
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    public static SeckillOrderStatusEnum fromCode(String code) {
        return Arrays
                .stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid seckill order status: '" + code + "', valid values: "
                                        + Arrays.toString(values())
                        )
                );
    }
}
