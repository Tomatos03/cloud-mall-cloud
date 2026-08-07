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

/**
 * 买家商家聊天会话实体
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_im_session")
public class ChatSessionDO implements Serializable {

    /** 会话ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 买家用户ID */
    private Long buyerId;

    /** 商家用户ID */
    private Long merchantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;

    /**
     * 判断用户是否为会话成员。
     *
     * @param userId 用户ID
     * @return true 表示用户是买家或商家成员
     */
    public boolean isMember(Long userId) {
        return userId.equals(buyerId) || userId.equals(merchantId);
    }
}
