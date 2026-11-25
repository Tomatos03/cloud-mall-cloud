package com.cloudmall.im.handler;

import java.io.IOException;
import java.util.Map;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.cloudmall.im.api.enums.MessageTypeEnum;
import com.cloudmall.im.api.model.ws.MessageReq;
import com.cloudmall.im.application.IConnectionHeartbeatManager;
import com.cloudmall.im.enums.ReadStatusEnum;
import com.cloudmall.im.infrastructure.store.IPresenceStore;
import com.cloudmall.im.infrastructure.support.IInstanceIdProvider;
import com.cloudmall.im.infrastructure.websocket.IWebSocketSessionRegistry;
import com.cloudmall.im.model.bo.UserRoute;
import com.cloudmall.im.model.entity.ChatMessageDO;
import com.cloudmall.im.model.entity.ChatSessionDO;
import com.cloudmall.im.service.IChatMessageService;
import com.cloudmall.im.service.IChatSessionService;

/**
 * WebSocket 聊天处理器
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final String ERROR_TYPE = "error";

    private final IWebSocketSessionRegistry sessionRegistry;
    private final IPresenceStore presenceStore;
    private final IInstanceIdProvider instanceIdProvider;
    private final IConnectionHeartbeatManager heartbeatManager;
    private final IChatMessageService chatMessageService;
    private final IChatSessionService chatSessionService;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            log.warn("WebSocket 连接缺少 userId 属性，sessionId={}", session.getId());
            return;
        }
        sessionRegistry.register(userId, session);
        presenceStore.register(userId, route(session));
        log.info("用户 {} 已连接 WebSocket，sessionId: {}", userId, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Long senderId = (Long) session.getAttributes().get("userId");
        if (senderId == null) {
            log.warn("WebSocket 消息处理失败：未找到发送者 ID");
            return;
        }

        heartbeatManager.record(session);
        heartbeatManager.refreshPresence(senderId, session.getId());

        MessageReq messageReq;
        try {
            messageReq = objectMapper.readValue(message.getPayload(), MessageReq.class);
        } catch (JsonProcessingException e) {
            log.warn("WebSocket 消息解析失败: {}", e.getMessage());
            sendError(session, "消息格式错误");
            return;
        }
        Long sessionId = messageReq.getSessionId();
        if (sessionId == null || messageReq.getType() == null || StrUtil.isBlank(messageReq.getContent())) {
            sendError(session, "消息内容不能为空");
            return;
        }

        ChatSessionDO chatSession = chatSessionService.getById(sessionId);
        if (chatSession == null || !chatSession.isMember(senderId)) {
            log.warn("用户 {} 向会话 {} 发送消息被拒绝：会话不存在或非成员", senderId, sessionId);
            sendError(session, "会话不存在或无权访问");
            return;
        }

        Long receiverId = senderId.equals(chatSession.getBuyerId())
                ? chatSession.getMerchantId()
                : chatSession.getBuyerId();

        ChatMessageDO chatMessage = ChatMessageDO.builder()
                                                 .sessionId(sessionId)
                                                 .senderId(senderId)
                                                 .content(messageReq.getContent())
                                                 .type(messageReq.getType())
                                                 .isRead(ReadStatusEnum.UNREAD.getValue())
                                                 .build();
        chatMessageService.saveWithFileEvent(chatMessage, resolveFileUrl(messageReq), receiverId);
        if (chatSession.isHiddenBy(receiverId)) {
            chatSessionService.updateHidden(chatSession, receiverId, false);
        }
        log.info("消息已保存，发送者: {}, 接收者: {}", senderId, receiverId);
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) {
        heartbeatManager.record(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessionRegistry.unregister(userId, session);
            presenceStore.unregisterIfMatch(userId, route(session));
            log.info("用户 {} 已断开 WebSocket，状态码: {}", userId, status.getCode());
        }
    }

    private UserRoute route(WebSocketSession session) {
        return new UserRoute(instanceIdProvider.get(), session.getId(), System.currentTimeMillis());
    }

    private static String resolveFileUrl(MessageReq messageReq) {
        if (!isMediaMessage(messageReq.getType())) {
            return null;
        }
        return messageReq.getContent();
    }

    private static boolean isMediaMessage(MessageTypeEnum type) {
        return MessageTypeEnum.IMAGE == type || MessageTypeEnum.VIDEO == type;
    }

    private void sendError(WebSocketSession session, String content) throws IOException {
        if (session.isOpen()) {
            Map<String, String> error = Map.of("type", ERROR_TYPE, "content", content);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
        }
    }
}
