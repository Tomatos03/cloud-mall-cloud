package com.cloudmall.im.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.cloudmall.common.model.CursorResult;
import com.cloudmall.im.entity.ChatMessageDO;
import com.cloudmall.im.model.req.MessageCursorReq;
import com.cloudmall.im.model.resp.MessageResp;

/**
 * 聊天消息服务接口
 *
 * @author : Tomatos
 * @date : 2026/8
 */
public interface IChatMessageService extends IService<ChatMessageDO> {

    /**
     * 游标分页获取会话消息历史（ID 游标，第一页为最新消息）。
     *
     * @param req 游标查询请求
     * @return 消息游标分页数据
     */
    CursorResult<MessageResp> cursorMessageHistory(MessageCursorReq req);

    /**
     * 获取会话最后一条消息。
     *
     * @param sessionId 会话ID
     * @return 最后一条消息，无消息时返回 null
     */
    ChatMessageDO getLastMessage(Long sessionId);

    /**
     * 统计会话未读消息数（排除自己发送的）。
     *
     * @param sessionId 会话ID
     * @param currentUserId 当前用户ID
     * @return 未读消息数
     */
    Long countUnread(Long sessionId, Long currentUserId);

    /**
     * 将会话中他人发送的消息标记为已读。
     *
     * @param sessionId 会话ID
     * @param excludeSenderId 排除的发送者（当前用户自己）
     * @return 更新的记录数
     */
    int markRead(Long sessionId, Long excludeSenderId);
}
