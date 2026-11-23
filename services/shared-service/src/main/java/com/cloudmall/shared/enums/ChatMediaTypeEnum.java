package com.cloudmall.shared.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 聊天附件类型。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum ChatMediaTypeEnum {
    /** 图片 */
    IMAGE("image", "图片"),

    /** 视频 */
    VIDEO("video", "视频");
    /** JSON 序列化值 */
    @JsonValue
    private final String code;
    /** 类型描述 */
    private final String desc;
}
