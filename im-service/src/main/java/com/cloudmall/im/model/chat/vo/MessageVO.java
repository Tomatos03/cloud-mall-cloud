package com.cloudmall.im.model.chat.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息视图对象
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageVO {
    private Long sessionId;
    private Long userId;
    /**
     * 消息内容（文本或图片URL）
     */
    private String content;
    private String type;

    /**
     * 消息时间 (ISO 8601格式)
     */
    private String time;
}
