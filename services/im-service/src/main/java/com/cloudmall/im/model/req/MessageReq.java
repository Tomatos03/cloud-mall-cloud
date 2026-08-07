package com.cloudmall.im.model.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.im.enums.MessageTypeEnum;

/**
 * WebSocket 发送消息请求
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageReq {

    /** 会话ID */
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    /** 消息内容（文本或图片URL） */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /** 消息类型 */
    @NotNull(message = "消息类型不能为空")
    private MessageTypeEnum type;
}
