package com.cloudmall.im.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.cloudmall.im.convert.ChatMessageConverter;
import com.cloudmall.im.entity.ChatMessageDO;
import com.cloudmall.im.entity.ChatSessionDO;
import com.cloudmall.im.manager.WebSocketSessionManager;
import com.cloudmall.im.model.req.MessageReq;
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
    /** 心跳超时阈值：超过该时长未响应即视为半开连接 */
    private static final Duration STALE_TIMEOUT = Duration.ofSeconds(90);

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

        webSocketSessionManager.recordHeartbeat(senderId);

        MessageReq messageReq = JSONUtil.toBean(message.getPayload(), MessageReq.class);
        Long sessionId = messageReq.getSessionId();

        // 会话必须已存在且当前用户为会话成员
        ChatSessionDO chatSession = chatSessionService.getById(sessionId);
        if (chatSession == null || !chatSession.isMember(senderId)) {
            log.warn("用户 {} 向会话 {} 发送消息被拒绝：会话不存在或非成员", senderId, sessionId);
            sendError(session, "会话不存在或无权访问");
            return;
        }

        // 滑动过期：发送消息视为活跃，续期在线状态
        webSocketSessionManager.refreshSession(senderId);

        Long receiverId = senderId.equals(chatSession.getBuyerId())
                ? chatSession.getMerchantId()
                : chatSession.getBuyerId();
        boolean receiverOnline = webSocketSessionManager.isOnline(receiverId);

        ChatMessageDO chatMessage = ChatMessageDO.builder()
                                                 .sessionId(sessionId)
                                                 .senderId(senderId)
                                                 .content(messageReq.getContent())
                                                 .type(messageReq.getType())
                                                 .isRead(receiverOnline)
                                                 .build();
        chatMessageService.save(chatMessage);

        if (receiverOnline) {
            WebSocketSession receiverSession = webSocketSessionManager.getSession(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                MessageResp resp = messageConverter.toResp(chatMessage);
                try {
                    receiverSession.sendMessage(new TextMessage(JSONUtil.toJsonStr(resp)));
                    log.info("消息已转发给在线用户 {}", receiverId);
                } catch (IOException e) {
                    // 发送失败说明接收方连接已不可用，立即清理
                    webSocketSessionManager.removeSession(receiverId);
                    log.warn("向用户 {} 推送消息失败，已清理会话: {}", receiverId, e.getMessage());
                }
            } else {
                // Redis 标记在线但本地连接不可用，说明半开连接或清理回调尚未执行，主动收敛在线状态
                webSocketSessionManager.removeSession(receiverId);
                log.warn("用户 {} 标记在线但本地连接不可用，已清理在线状态", receiverId);
            }
        }
        log.info("消息已保存，发送者: {}, 接收者: {}", senderId, receiverId);
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            webSocketSessionManager.recordHeartbeat(userId);
        }
    }

    /**
     * 定时向所有在线连接发送 ping，探测连接是否存活（浏览器收到 ping 会自动回 pong）。
     */
    @Scheduled(fixedDelay = 30_000, initialDelay = 30_000)
    public void sendHeartbeatPings() {
        for (WebSocketSession session : webSocketSessionManager.getAllSessions()) {
            if (session instanceof StandardWebSocketSession standardSession) {
                jakarta.websocket.Session nativeSession = standardSession.getNativeSession();
                if (nativeSession == null) {
                    continue;
                }
                try {
                    nativeSession.getBasicRemote().sendPing(ByteBuffer.wrap(new byte[0]));
                } catch (IOException e) {
                    log.debug("向 session {} 发送心跳 ping 失败", session.getId());
                }
            }
        }
    }

    /**
     * 定时清理心跳超时的半开连接（客户端已断连但服务端未感知）。
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void cleanupStaleSessions() {
        List<Long> staleUserIds = webSocketSessionManager.findStaleSessionUserIds(STALE_TIMEOUT);
        for (Long userId : staleUserIds) {
            WebSocketSession session = webSocketSessionManager.getSession(userId);
            if (session != null && session.isOpen()) {
                try {
                    session.close(CloseStatus.GOING_AWAY);
                } catch (IOException e) {
                    log.warn("关闭用户 {} 的半开连接失败: {}", userId, e.getMessage());
                }
            }
            webSocketSessionManager.removeSession(userId);
            log.info("用户 {} 心跳超时，已清理半开连接", userId);
        }
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
     * 向发送方回写错误帧。
     */
    private void sendError(WebSocketSession session, String content) throws IOException {
        if (session.isOpen()) {
            Map<String, String> error = Map.of("type", ERROR_TYPE, "content", content);
            session.sendMessage(new TextMessage(JSONUtil.toJsonStr(error)));
        }
    }
}
