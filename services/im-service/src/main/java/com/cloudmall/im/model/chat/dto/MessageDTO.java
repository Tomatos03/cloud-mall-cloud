package com.cloudmall.im.model.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送消息数据传输对象
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {
    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    /**
     * 消息内容（不能为空）
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    @NotBlank
    private String type;

    private Long receiverId;
}
