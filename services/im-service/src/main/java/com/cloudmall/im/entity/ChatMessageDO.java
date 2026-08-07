package com.cloudmall.im.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.im.enums.MessageTypeEnum;

/**
 * 买家商家聊天消息实体
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_im_message")
public class ChatMessageDO implements Serializable {

    /** 消息ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    private Long sessionId;

    /** 发送者用户ID */
    private Long senderId;

    /** 消息内容（文本或图片URL） */
    private String content;

    /** 消息类型 */
    private MessageTypeEnum type;

    /** 是否已读（0=否 1=是） */
    private Boolean isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Boolean deleted;
}
