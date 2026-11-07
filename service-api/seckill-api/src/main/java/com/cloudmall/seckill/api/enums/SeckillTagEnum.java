package com.cloudmall.seckill.api.enums;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 秒杀事件类型（RocketMQ Tag）
 * <p>
 * seckill-service 发布 SECKILL_ORDERS 消息时写入 tag，consumer-service 按 tag 分发消费。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum SeckillTagEnum {
    /** 抢购成功，需异步建真实订单 */
    ORDER_CREATE("SeckillOrderCreate");
    /** 消息 tag 值 */
    private final String tag;
    /**
     * 按 tag 值查找枚举。
     *
     * @param tag 消息 tag 值
     * @return 匹配枚举（未命中返回 null，由调用方决定跳过或告警）
     */
    public static SeckillTagEnum from(String tag) {
        return Arrays.stream(values())
                     .filter(value -> value.tag.equals(tag))
                     .findFirst()
                     .orElse(null);
    }
}
