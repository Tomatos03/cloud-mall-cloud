package com.cloudmall.im.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息已读状态枚举
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Getter
@AllArgsConstructor
public enum ReadStatusEnum {
    UNREAD(false, "未读"),
    READ(true, "已读");
    private final Boolean value;
    private final String desc;
}
