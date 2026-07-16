package com.cloudmall.im.model.chat.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天消息实体
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：0=文本, 1=图片
     */
    private String type;

    private Boolean isRead;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}