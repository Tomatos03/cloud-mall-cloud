package com.cloudmall.im.application;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.common.model.CursorResult;
import com.cloudmall.im.model.req.MessageCursorReq;
import com.cloudmall.im.model.req.SessionPageReq;
import com.cloudmall.im.model.resp.ChatSessionResp;
import com.cloudmall.im.model.resp.CreateSessionResp;
import com.cloudmall.im.model.resp.MessageResp;

/**
 * 聊天应用服务接口
 *
 * @author : Tomatos
 * @date : 2026/8
 */
public interface IChatAppService {

    /**
     * 创建买家与商家的会话（已存在则复用）。
     *
     * @param buyerId 买家用户ID
     * @param storeId 店铺ID
     * @return 会话ID
     */
    CreateSessionResp createChatSession(Long buyerId, Long storeId);

    /**
     * 分页获取当前用户的会话列表。
     *
     * @param currentUserId 当前用户ID
     * @param req 分页请求
     * @return 会话分页数据
     */
    IPage<ChatSessionResp> pageChatSession(Long currentUserId, SessionPageReq req);

    /**
     * 游标分页获取会话消息历史（当前用户必须是会话成员）。
     *
     * @param currentUserId 当前用户ID
     * @param req 游标查询请求
     * @return 消息游标分页数据
     */
    CursorResult<MessageResp> cursorMessageHistory(Long currentUserId, MessageCursorReq req);

    /**
     * 将会话中对方发送的消息标记为已读。
     *
     * @param currentUserId 当前用户ID
     * @param sessionId 会话ID
     */
    void markReadForChatSession(Long currentUserId, Long sessionId);

    /**
     * 设置当前用户对会话的置顶状态。
     *
     * @param currentUserId 当前用户ID
     * @param sessionId 会话ID
     * @param pinned 是否置顶
     */
    void setSessionPinned(Long currentUserId, Long sessionId, Boolean pinned);

    /**
     * 删除当前用户的会话（仅从当前用户列表隐藏，不影响对方）。
     *
     * @param currentUserId 当前用户ID
     * @param sessionId 会话ID
     */
    void deleteChatSession(Long currentUserId, Long sessionId);

    /**
     * 批量删除当前用户的会话（仅从当前用户列表隐藏，不影响对方）。
     *
     * @param currentUserId 当前用户ID
     * @param sessionIds 会话ID列表
     */
    void deleteChatSessions(Long currentUserId, List<Long> sessionIds);
}
