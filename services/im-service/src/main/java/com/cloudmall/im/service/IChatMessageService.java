package com.cloudmall.im.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import com.cloudmall.im.entity.ChatMessageDO;
import com.cloudmall.im.model.req.MessagePageReq;
import com.cloudmall.im.model.resp.MessageResp;

/**
 * 聊天消息服务接口
 *
 * @author : Tomatos
 * @date : 2026/8
 */
public interface IChatMessageService extends IService<ChatMessageDO> {

    /**
     * 分页获取会话历史消息（倒序分页，第 1 页为最新一页）。
     *
     * @param req 分页查询请求
     * @return 消息分页数据
     */
    IPage<MessageResp> pageMessageHistory(MessagePageReq req);

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
}
