package com.cloudmall.im.model.chat.application;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.im.common.entity.PageParamsDTO;
import com.cloudmall.im.model.chat.dto.CreateSessionResult;
import com.cloudmall.im.model.chat.entity.ChatMessage;
import com.cloudmall.im.model.chat.entity.ChatSession;
import com.cloudmall.im.model.chat.service.IChatMessageService;
import com.cloudmall.im.model.chat.service.IChatSessionService;
import com.cloudmall.im.model.chat.vo.ChatSessionVO;
import com.cloudmall.im.model.store.IStoreService;
import com.cloudmall.im.model.store.Store;
import com.cloudmall.im.model.system.user.IUserService;
import com.cloudmall.im.model.system.user.entity.User;
import com.cloudmall.im.security.context.AuthUserContext;

/**
 * 聊天应用服务实现
 *
 * @author : Tomatos
 * @date : 2026/2/17
 */
@Service
@RequiredArgsConstructor
public class ChatAppService implements IChatAppService {
    private final IChatMessageService chatMessageService;
    private final IChatSessionService chatSessionService;
    private final IStoreService storeService;
    private final IUserService userService;

    @Override
    public IPage<ChatSessionVO> pageChatSession(PageParamsDTO paramsDTO) {
        Page<ChatSession> page = new Page<>(paramsDTO.getPage(), paramsDTO.getPageSize());
        var wrapper = Wrappers.<ChatSession>lambdaQuery();
        Long userId = AuthUserContext.getUserId();
        wrapper.eq(ChatSession::getAgentId, userId)
               .or()
               .eq(ChatSession::getBuyerId, userId)
               .orderByDesc(ChatSession::getCreateTime);

        return chatSessionService.page(page, wrapper)
                                 .convert(this::conversationVO);
    }

    private ChatSessionVO conversationVO(ChatSession chatSession) {
        Long receiverId = determineReceiverId(chatSession);
        User receiverUser = userService.getById(receiverId);

        Long unReadCount = chatMessageService.countUnreadMessages(chatSession.getId());
        ChatMessage lastMessage = chatMessageService.getLastMessage(chatSession.getId());
        ChatSessionVO.ChatSessionVOBuilder builder = ChatSessionVO.builder()
                                                                   .id(chatSession.getId())
                                                                   .userId(receiverId)
                                                                   .name(receiverUser != null ? receiverUser.getNickname() : null)
                                                                   .avatar(receiverUser != null ? receiverUser.getAvatarUrl() : null)
                                                                   .unreadCount(unReadCount);

        if (lastMessage != null) {
            builder.lastMessageContent(lastMessage.getContent())
                   .lastTime(lastMessage.getCreateTime());
        }
        return builder.build();
    }

    private static Long determineReceiverId(ChatSession chatSession) {
        Long currentUserId = AuthUserContext.getUserId();
        if (currentUserId == null) {
            return chatSession.getBuyerId();
        }
        return currentUserId.equals(chatSession.getBuyerId())
                ? chatSession.getAgentId()
                : chatSession.getBuyerId();
    }

    @Override
    public void markReadForChatSession(Long sessionId) {
        ChatSession chatSession = chatSessionService.getById(sessionId);
        AssertUtils.notNull(chatSession, BizErrorCode.CONVERSATION_NOT_EXIST);

        Long currentUserId = AuthUserContext.getUserId();
        Long senderId = currentUserId != null && currentUserId.equals(chatSession.getBuyerId())
                ? chatSession.getAgentId()
                : chatSession.getBuyerId();

        chatMessageService.lambdaUpdate()
                          .eq(ChatMessage::getSessionId, sessionId)
                          .eq(ChatMessage::getSenderId, senderId)
                          .set(ChatMessage::getIsRead, true)
                          .update();
    }

    @Override
    public CreateSessionResult createChatSession(Long storeId) {
        Store store = storeService.getById(storeId);
        AssertUtils.notNull(store, BizErrorCode.STORE_NOT_EXIST);

        Long merchantId = store.getUserId();
        Long userId = AuthUserContext.getUserId();
        ChatSession chatSession = chatSessionService.lambdaQuery()
                                                    .eq(ChatSession::getBuyerId, userId)
                                                    .eq(ChatSession::getAgentId, merchantId)
                                                    .one();
        if (chatSession == null) {
            chatSession = ChatSession.builder()
                                     .buyerId(userId)
                                     .agentId(merchantId)
                                     .createTime(LocalDateTime.now())
                                     .build();
            chatSessionService.save(chatSession);
        }

        CreateSessionResult result = new CreateSessionResult();
        result.setSessionId(chatSession.getId());
        return result;
    }
}
