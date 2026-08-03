package com.cloudmall.auth.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Outbox 发布状态枚举
 * PENDING=待发布 PUBLISHED=已发布 DEAD=死信
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum OutboxPublishStatusEnum {
    PENDING("PENDING", "待发布"),
    PUBLISHED("PUBLISHED", "已发布"),
    DEAD("DEAD", "死信");
    @EnumValue
    private final String code;
    private final String desc;
}
