package com.cloudmall.im.handler;

import java.io.IOException;
import java.util.Map;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.cloudmall.im.convert.ChatMessageConverter;
import com.cloudmall.im.entity.ChatMessageDO;
import com.cloudmall.im.entity.ChatSessionDO;
import com.cloudmall.im.model.req.MessageDTO;
import com.cloudmall.im.model.resp.MessageResp;
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

    private final WebSocketSessionManager webSocketSessionManager;
    private final IChatMessageService chatMessageService;
    private final IChatSessionService chatSessionService;
    private final ChatMessageConverter messageConverter;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        webSocketSessionManager.addSession(userId, session);
        log.info("用户 {} 已连接 WebSocket，sessionId: {}", userId, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Long senderId = (Long) session.getAttributes().get("userId");
        if (senderId == null) {
            log.warn("WebSocket 消息处理失败：未找到发送者 ID");
            return;
        }

        MessageDTO messageDTO = JSONUtil.toBean(message.getPayload(), MessageDTO.class);
        Long sessionId = messageDTO.getSessionId();

        // 会话必须已存在且当前用户为会话成员
        ChatSessionDO chatSession = chatSessionService.getById(sessionId);
        if (chatSession == null || !isMember(chatSession, senderId)) {
            log.warn("用户 {} 向会话 {} 发送消息被拒绝：会话不存在或非成员", senderId, sessionId);
            sendError(session, "会话不存在或无权访问");
            return;
        }

        Long receiverId = senderId.equals(chatSession.getBuyerId())
                ? chatSession.getAgentId()
                : chatSession.getBuyerId();
        boolean receiverOnline = webSocketSessionManager.isOnline(receiverId);

        ChatMessageDO chatMessage = ChatMessageDO.builder()
                                                 .sessionId(sessionId)
                                                 .senderId(senderId)
                                                 .content(messageDTO.getContent())
                                                 .type(messageDTO.getType())
                                                 .isRead(receiverOnline)
                                                 .build();
        chatMessageService.save(chatMessage);

        if (receiverOnline) {
            WebSocketSession receiverSession = webSocketSessionManager.getSession(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                MessageResp resp = messageConverter.toResp(chatMessage);
                receiverSession.sendMessage(new TextMessage(JSONUtil.toJsonStr(resp)));
                log.info("消息已转发给在线用户 {}", receiverId);
            }
        }
        log.info("消息已保存，发送者: {}, 接收者: {}", senderId, receiverId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            webSocketSessionManager.removeSession(userId);
            log.info("用户 {} 已断开 WebSocket，状态码: {}", userId, status.getCode());
        }
    }

    /**
     * 判断用户是否为会话成员。
     */
    private static boolean isMember(ChatSessionDO chatSession, Long userId) {
        return userId.equals(chatSession.getBuyerId()) || userId.equals(chatSession.getAgentId());
    }

    /**
     * 向发送方回写错误帧。
     */
    private void sendError(WebSocketSession session, String content) throws IOException {
        if (session.isOpen()) {
            Map<String, String> error = Map.of("type", ERROR_TYPE, "content", content);
            session.sendMessage(new TextMessage(JSONUtil.toJsonStr(error)));
        }
    }
}
