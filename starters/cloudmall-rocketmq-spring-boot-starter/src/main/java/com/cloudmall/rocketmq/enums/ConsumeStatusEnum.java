package com.cloudmall.rocketmq.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息消费状态枚举
 * CONSUMED=已消费 DEAD=死信
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum ConsumeStatusEnum {
    CONSUMED("CONSUMED", "已消费"),
    DEAD("DEAD", "死信");
    private final String code;
    private final String desc;
}
