package com.cloudmall.im.model.resp;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息视图对象
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResp {

    /** 消息ID */
    private Long id;

    /** 会话ID */
    private Long sessionId;

    /** 发送者用户ID */
    private Long userId;

    /** 消息内容（文本或图片URL） */
    private String content;

    /** 消息类型（text/image） */
    private String type;

    /** 消息时间 */
    private LocalDateTime time;
}
