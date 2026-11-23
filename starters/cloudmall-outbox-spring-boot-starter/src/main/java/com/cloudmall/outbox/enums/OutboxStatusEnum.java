package com.cloudmall.outbox.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Outbox 事件状态。
 * <p>
 * PENDING=待处理 PROCESSING=处理中 DONE=已完成 FAILED=失败。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum OutboxStatusEnum {
    /** 待处理 */
    PENDING("PENDING", "待处理"),

    /** 处理中 */
    PROCESSING("PROCESSING", "处理中"),

    /** 已完成 */
    DONE("DONE", "已完成"),

    /** 失败 */
    FAILED("FAILED", "失败");
    /** 状态码（数据库存储值） */
    @EnumValue
    private final String code;
    /** 状态描述 */
    private final String desc;
}
