package com.cloudmall.outbox.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Outbox 发布状态枚举
 * <p>
 * PENDING=待发布 PUBLISHED=已发布 DEAD=死信。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum OutboxPublishStatusEnum {
    /** 待发布 */
    PENDING("PENDING", "待发布"),

    /** 已发布 */
    PUBLISHED("PUBLISHED", "已发布"),

    /** 死信 */
    DEAD("DEAD", "死信");
    /** 状态码（数据库存储值） */
    @EnumValue
    private final String code;
    /** 状态描述 */
    private final String desc;
}
