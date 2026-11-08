package com.cloudmall.im.application;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.model.CursorResult;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.im.model.entity.ChatMessageDO;
import com.cloudmall.im.model.entity.ChatSessionDO;
import com.cloudmall.im.model.req.MessageCursorReq;
import com.cloudmall.im.model.req.SessionPageReq;
import com.cloudmall.im.model.resp.ChatSessionResp;
import com.cloudmall.im.model.resp.CreateSessionResp;
import com.cloudmall.im.model.resp.MessageResp;
import com.cloudmall.im.service.IChatMessageService;
import com.cloudmall.im.service.IChatSessionService;
import com.cloudmall.user.api.client.ShopClient;
import com.cloudmall.user.api.client.UserClient;
import com.cloudmall.user.api.model.resp.ShopResp;
import com.cloudmall.user.api.model.resp.UserResp;

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
        Asserts.notNull(buyerId, BizErrorEnum.NO_PERMISSION);
        ShopResp shop = Asserts.requireNonNull(shopClient.getById(storeId), BizErrorEnum.STORE_NOT_EXIST);

        ChatSessionDO session = chatSessionService.selectByBuyerAndMerchant(buyerId, shop.getUserId());
        if (session == null) {
            session = ChatSessionDO.builder()
                                   .buyerId(buyerId)
                                   .merchantId(shop.getUserId())
                                   .build();
            chatSessionService.save(session);
        } else if (session.isHiddenBy(buyerId)) {
            // 从店铺页重新发起会话时恢复被删除的会话
            chatSessionService.updateHidden(session, buyerId, false);
        }
        return CreateSessionResp.builder()
                                .sessionId(session.getId())
                                .build();
    }

    @Override
    public IPage<ChatSessionResp> pageChatSession(Long currentUserId, SessionPageReq req) {
        IPage<ChatSessionDO> page = chatSessionService.pageByMember(currentUserId, req);
        return page.convert(session -> toSessionResp(session, currentUserId));
    }

    @Override
    public CursorResult<MessageResp> cursorMessageHistory(Long currentUserId, MessageCursorReq req) {
        requireMember(req.getSessionId(), currentUserId);
        return chatMessageService.cursorMessageHistory(req);
    }

    @Override
    public void markReadForChatSession(Long currentUserId, Long sessionId) {
        requireMember(sessionId, currentUserId);
        chatMessageService.markRead(sessionId, currentUserId);
    }

    @Override
    public void setSessionPinned(Long currentUserId, Long sessionId, Boolean pinned) {
        ChatSessionDO session = requireMember(sessionId, currentUserId);
        chatSessionService.updatePinned(session, currentUserId, Boolean.TRUE.equals(pinned));
    }

    @Override
    public void deleteChatSession(Long currentUserId, Long sessionId) {
        ChatSessionDO session = requireMember(sessionId, currentUserId);
        chatSessionService.updateHidden(session, currentUserId, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChatSessions(Long currentUserId, List<Long> sessionIds) {
        if (CollUtil.isEmpty(sessionIds)) {
            return;
        }

        Set<Long> uniqueSessionIds = new LinkedHashSet<>(sessionIds);
        for (Long sessionId : uniqueSessionIds) {
            ChatSessionDO session = requireMember(sessionId, currentUserId);
            chatSessionService.updateHidden(session, currentUserId, true);
        }
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
                BizErrorEnum.CONVERSATION_NOT_EXIST
        );
        Asserts.isTrue(
                currentUserId != null && session.isMember(currentUserId),
                BizErrorEnum.CONVERSATION_NOT_MEMBER
        );
        return session;
    }

    /**
     * 构建会话响应对象（对方昵称/头像、未读数、最后一条消息）。
     */
    private ChatSessionResp toSessionResp(ChatSessionDO session, Long currentUserId) {
        Long otherId = currentUserId.equals(session.getBuyerId())
                ? session.getMerchantId()
                : session.getBuyerId();
        UserResp other = userClient.getById(otherId);
        ChatMessageDO lastMessage = chatMessageService.getLastMessage(session.getId());

        return ChatSessionResp.builder()
                              .id(session.getId())
                              .userId(otherId)
                              .name(other != null ? other.getNickname() : null)
                              .avatar(other != null ? other.getAvatar() : null)
                              .lastMessageContent(lastMessage != null ? lastMessage.getContent() : null)
                              .lastTime(lastMessage != null ? lastMessage.getCreateTime() : null)
                              .createTime(session.getCreateTime())
                              .unreadCount(chatMessageService.countUnread(session.getId(), currentUserId))
                              .pinned(session.isPinnedBy(currentUserId))
                              .build();
    }
}
