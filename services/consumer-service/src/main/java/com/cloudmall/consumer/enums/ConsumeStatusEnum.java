package com.cloudmall.consumer.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
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
    @EnumValue
    private final String code;
    private final String desc;
}
