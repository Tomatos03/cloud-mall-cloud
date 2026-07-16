package com.cloudmall.im.model.chat.vo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话视图对象
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSessionVO {
    /**
     * 会话ID
     */
    private Long id;

    /**
     * 对方用户ID
     */
    private Long userId;

    /**
     * 对方名称
     */
    private String name;

    /**
     * 对方头像URL
     */
    private String avatar;

    /**
     * 最后一条消息内容
     */
    private String lastMessageContent;

    /**
     * 最后一条消息时间 (ISO 8601格式)
     */
    private LocalDateTime lastTime;

    private Long unreadCount;
}