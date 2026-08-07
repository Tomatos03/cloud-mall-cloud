package com.cloudmall.im.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 聊天消息类型枚举
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Getter
@AllArgsConstructor
public enum MessageTypeEnum {
    /** 文本消息 */
    TEXT("text", "文本"),

    /** 图片消息 */
    IMAGE("image", "图片");
    /** 数据库存储值及 JSON 序列化值 */
    @EnumValue
    @JsonValue
    private final String code;
    /** 类型描述 */
    private final String desc;
    /**
     * 根据编码查找消息类型。
     *
     * @param code 类型编码，如 "text"
     * @return 匹配的消息类型
     * @throws IllegalArgumentException 编码为 null 或非法时抛出
     */
    @JsonCreator
    public static MessageTypeEnum fromCode(String code) {
        return Arrays
                .stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid message type: '" + code + "', valid values: "
                                        + Arrays.toString(values())
                        )
                );
    }
}
