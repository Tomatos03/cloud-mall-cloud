package com.cloudmall.im.application;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.auth.api.client.UserClient;
import com.cloudmall.auth.api.response.UserInfoResp;
import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.im.entity.ChatMessageDO;
import com.cloudmall.im.entity.ChatSessionDO;
import com.cloudmall.im.model.req.MessagePageReq;
import com.cloudmall.im.model.req.SessionPageReq;
import com.cloudmall.im.model.resp.ChatSessionResp;
import com.cloudmall.im.model.resp.CreateSessionResp;
import com.cloudmall.im.model.resp.MessageResp;
import com.cloudmall.im.service.IChatMessageService;
import com.cloudmall.im.service.IChatSessionService;
import com.cloudmall.user.api.client.ShopClient;
import com.cloudmall.user.api.response.ShopResp;

/**
 * 聊天应用服务实现
 * <p>
 * 负责跨服务编排（店铺/用户信息 Feign 查询）与会话成员校验。
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Service
@RequiredArgsConstructor
public class ChatAppService implements IChatAppService {

    private final IChatMessageService chatMessageService;
    private final IChatSessionService chatSessionService;
    private final ShopClient shopClient;
    private final UserClient userClient;

    @Override
    public CreateSessionResp createChatSession(Long buyerId, Long storeId) {
        Asserts.notNull(buyerId, BizError.NO_PERMISSION);
        ShopResp shop = Asserts.requireNonNull(shopClient.getById(storeId), BizError.STORE_NOT_EXIST);

        ChatSessionDO session = chatSessionService.lambdaQuery()
                                                  .eq(ChatSessionDO::getBuyerId, buyerId)
                                                  .eq(ChatSessionDO::getAgentId, shop.getUserId())
                                                  .one();
        if (session == null) {
            session = ChatSessionDO.builder()
                                   .buyerId(buyerId)
                                   .agentId(shop.getUserId())
                                   .build();
            chatSessionService.save(session);
        }
        return CreateSessionResp.builder()
                                .sessionId(session.getId())
                                .build();
    }

    @Override
    public IPage<ChatSessionResp> pageChatSession(Long currentUserId, SessionPageReq req) {
        IPage<ChatSessionDO> page = chatSessionService
                .lambdaQuery()
                .and(
                        w -> w.eq(ChatSessionDO::getBuyerId, currentUserId)
                              .or()
                              .eq(ChatSessionDO::getAgentId, currentUserId)
                )
                .orderByDesc(ChatSessionDO::getCreateTime)
                .page(new Page<>(req.getPage(), req.getPageSize()));
        return page.convert(session -> toSessionResp(session, currentUserId));
    }

    @Override
    public IPage<MessageResp> pageMessageHistory(Long currentUserId, MessagePageReq req) {
        requireMember(req.getSessionId(), currentUserId);
        return chatMessageService.pageMessageHistory(req);
    }

    @Override
    public void markReadForChatSession(Long currentUserId, Long sessionId) {
        requireMember(sessionId, currentUserId);
        chatMessageService.lambdaUpdate()
                          .eq(ChatMessageDO::getSessionId, sessionId)
                          .ne(ChatMessageDO::getSenderId, currentUserId)
                          .set(ChatMessageDO::getIsRead, true)
                          .update();
    }

    /**
     * 校验当前用户是否为会话成员，返回会话实体。
     *
     * @param sessionId 会话ID
     * @param currentUserId 当前用户ID
     * @return 会话实体
     */
    private ChatSessionDO requireMember(Long sessionId, Long currentUserId) {
        ChatSessionDO session = Asserts.requireNonNull(
                chatSessionService.getById(sessionId),
                BizError.CONVERSATION_NOT_EXIST
        );
        Asserts.isTrue(
                currentUserId != null
                        && (currentUserId.equals(session.getBuyerId()) || currentUserId.equals(session.getAgentId())),
                BizError.CONVERSATION_NOT_MEMBER
        );
        return session;
    }

    /**
     * 构建会话响应对象（对方昵称/头像、未读数、最后一条消息）。
     */
    private ChatSessionResp toSessionResp(ChatSessionDO session, Long currentUserId) {
        Long otherId = currentUserId.equals(session.getBuyerId())
                ? session.getAgentId()
                : session.getBuyerId();
        UserInfoResp other = userClient.getById(otherId);
        ChatMessageDO lastMessage = chatMessageService.getLastMessage(session.getId());

        return ChatSessionResp.builder()
                              .id(session.getId())
                              .userId(otherId)
                              .name(other != null ? other.getNickname() : null)
                              .avatar(other != null ? other.getAvatar() : null)
                              .lastMessageContent(lastMessage != null ? lastMessage.getContent() : null)
                              .lastTime(lastMessage != null ? lastMessage.getCreateTime() : null)
                              .unreadCount(chatMessageService.countUnread(session.getId(), currentUserId))
                              .build();
    }
}
