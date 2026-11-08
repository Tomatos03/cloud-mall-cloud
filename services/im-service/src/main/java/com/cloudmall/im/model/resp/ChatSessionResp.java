package com.cloudmall.im.model.resp;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话视图对象
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionResp {

    /** 会话ID */
    private Long id;

    /** 对方用户ID */
    private Long userId;

    /** 对方昵称 */
    private String name;

    /** 对方头像URL */
    private String avatar;

    /** 最后一条消息内容 */
    private String lastMessageContent;

    /** 最后一条消息时间 */
    private LocalDateTime lastTime;

    /** 会话创建时间 */
    private LocalDateTime createTime;

    /** 未读消息数 */
    private Long unreadCount;

    /** 当前用户是否置顶 */
    private Boolean pinned;
}
