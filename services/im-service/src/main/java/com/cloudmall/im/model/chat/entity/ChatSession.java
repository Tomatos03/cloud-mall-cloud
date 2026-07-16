package com.cloudmall.im.model.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天会话实体
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("chat_session")
public class ChatSession {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 买家ID
     */
    private Long buyerId;

    /**
     * 店铺ID
     */
    private Long agentId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
