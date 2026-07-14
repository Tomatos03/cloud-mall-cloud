package com.cloudmall.im.handler;

import cn.hutool.json.JSONUtil;
import com.cloudmall.im.model.chat.dto.MessageDTO;
import com.cloudmall.im.model.chat.entity.ChatMessage;
import com.cloudmall.im.model.chat.entity.ChatSession;
import com.cloudmall.im.model.chat.service.IChatMessageService;
import com.cloudmall.im.model.chat.service.IChatSessionService;
import com.cloudmall.im.model.chat.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * WebSocket聊天处理器
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final WebSocketSessionManager webSocketSessionManager;
    private final IChatMessageService chatMessageService;
    private final IChatSessionService chatSessionService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Map<String, Object> attributes = session.getAttributes();
        Long userId = (Long) attributes.get("userId");
        webSocketSessionManager.addSession(userId, session);
        log.info("用户 {} 已连接 WebSocket，sessionId: {}", userId, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        MessageDTO messageDTO = JSONUtil.toBean(message.getPayload(), MessageDTO.class);
        Long receiverId = messageDTO.getReceiverId();
        Long sessionId = messageDTO.getSessionId();
        Map<String, Object> attributes = session.getAttributes();
        Long senderId = (Long) attributes.get("userId");
        if (senderId == null) {
            log.warn("WebSocket消息处理失败：未找到发送者ID");
            return;
        }

        boolean targetUserOnline = webSocketSessionManager.isOnline(messageDTO.getReceiverId(), session);
        ChatSession chatSession = chatSessionService.getById(sessionId);
        if (chatSession == null) {
            chatSessionService.save(
                    ChatSession.builder()
                               .buyerId(senderId)
                               .agentId(receiverId)
                               .build()
            );
        }

        if (targetUserOnline) {
            WebSocketSession userSession = webSocketSessionManager.getSession(receiverId);
            if (userSession != null && userSession.isOpen()) {
                MessageVO messageVO = new MessageVO();
                messageVO.setContent(messageDTO.getContent());
                messageVO.setTime(LocalDateTime.now().toString());
                messageVO.setType(messageDTO.getType());
                messageVO.setUserId(senderId);
                messageVO.setSessionId(sessionId);

                userSession.sendMessage(new TextMessage(JSONUtil.toJsonStr(messageVO)));
                log.info("消息已发送给在线用户 {}", receiverId);
            }
        }

        chatMessageService.save(
                ChatMessage.builder()
                           .content(messageDTO.getContent())
                           .sessionId(sessionId)
                           .senderId(senderId)
                           .isRead(targetUserOnline)
                           .build()
        );

        log.info("消息已保存，发送者: {}, 接收者: {}", senderId, receiverId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes()
                                    .get("userId");
        if (userId != null) {
            webSocketSessionManager.removeSession(userId);
            log.info("用户 {} 已断开 WebSocket，状态码: {}", userId, status.getCode());
        }
    }
}
