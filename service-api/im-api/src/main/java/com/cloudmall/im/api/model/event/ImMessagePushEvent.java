package com.cloudmall.im.api.model.event;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.im.api.enums.MessageTypeEnum;

/**
 * IM 消息推送事件
 * <p>
 * 用于 Outbox 到 MQ 的跨实例投递，messageId 作为幂等键，sessionId 作为顺序键。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImMessagePushEvent {
    private Long messageId;
    private Long sessionId;
    private Long senderId;
    private Long receiverId;
    private MessageTypeEnum type;
    private String content;
    private LocalDateTime createdAt;
}
