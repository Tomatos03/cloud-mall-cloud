package com.cloudmall.im.application;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.im.model.req.MessagePageReq;
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
     * 分页获取会话消息历史（当前用户必须是会话成员）。
     *
     * @param currentUserId 当前用户ID
     * @param req 分页请求
     * @return 消息分页数据
     */
    IPage<MessageResp> pageMessageHistory(Long currentUserId, MessagePageReq req);

    /**
     * 将会话中对方发送的消息标记为已读。
     *
     * @param currentUserId 当前用户ID
     * @param sessionId 会话ID
     */
    void markReadForChatSession(Long currentUserId, Long sessionId);
}
