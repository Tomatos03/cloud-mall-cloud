package com.cloudmall.order.api.event;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单事件类型（RocketMQ Tag）
 * <p>
 * order-service 发布 ORDER_EVENTS 消息时写入 tag，consumer-service 按 tag 分发消费。
 * 生产与消费两侧统一引用本枚举，避免魔法字符串漂移。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum OrderTagEnum {
    /** 支付成功 */
    PAID("OrderPaid"),

    /** 支付超时 */
    TIMEOUT("OrderTimeout"),

    /** 订单取消 */
    CANCELLED("OrderCancelled"),

    /** 全额退款 */
    FULLY_REFUNDED("OrderFullyRefunded");
    /** 消息 tag 值 */
    private final String tag;
    /**
     * 按 tag 值查找枚举。
     *
     * @param tag 消息 tag 值
     * @return 匹配枚举（未命中返回 null，由调用方决定跳过或告警）
     */
    public static OrderTagEnum from(String tag) {
        return Arrays.stream(values())
                     .filter(value -> value.tag.equals(tag))
                     .findFirst()
                     .orElse(null);
    }
}
