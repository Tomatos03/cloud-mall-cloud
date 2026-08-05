package com.cloudmall.product.api.event;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品事件类型（RocketMQ Tag）
 * <p>
 * product-service 发布 PRODUCT_EVENTS 消息时写入 tag，search-service 等消费方按 tag 分发消费。
 * 生产与消费两侧统一引用本枚举，避免魔法字符串漂移。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum ProductTagEnum {
    /** 商品创建 */
    CREATED("ProductCreated"),

    /** 商品更新 */
    UPDATED("ProductUpdated"),

    /** 商品删除 */
    DELETED("ProductDeleted");
    /** 消息 tag 值 */
    private final String tag;
    /**
     * 按 tag 值查找枚举。
     *
     * @param tag 消息 tag 值
     * @return 匹配枚举（未命中返回 null，由调用方决定跳过或告警）
     */
    public static ProductTagEnum from(String tag) {
        return Arrays.stream(values())
                     .filter(value -> value.tag.equals(tag))
                     .findFirst()
                     .orElse(null);
    }
}
