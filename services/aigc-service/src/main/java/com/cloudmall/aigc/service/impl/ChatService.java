package com.cloudmall.aigc.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import com.cloudmall.aigc.agent.GeneralAgent;
import com.cloudmall.aigc.agent.RouterAgent;
import com.cloudmall.aigc.manager.GeneratingSessionManager;
import com.cloudmall.aigc.model.req.ChatReq;
import com.cloudmall.aigc.model.resp.ChatResp;
import com.cloudmall.aigc.service.IChatService;

/**
 * AI 智能客服实现。
 * <p>
 * 流式聊天委托给 RouterAgent 进行意图分析与路由分发；
 * 纯文本非流式聊天直接由 GeneralAgent 处理，绕过意图路由。
 * 各业务智能体（GoodsAgent、OrderAgent 等）的流式生成逻辑由 AbstractAgent 统一管理，
 * ChatService 仅作为入口。
 *
 * @author : Tomatos
 * @date : 2026/7/21
 */
@Service
@RequiredArgsConstructor
public class ChatService implements IChatService {

    private final RouterAgent routerAgent;
    private final GeneratingSessionManager generatingSessionManager;
    private final GeneralAgent generalAgent;

    @Override
    public Flux<ChatResp> chat(ChatReq req) {
        return routerAgent.chat(req);
    }

    @Override
    public String chatText(ChatReq req) {
        return generalAgent.chatText(req);
    }

    @Override
    public void stop(String sessionId) {
        generatingSessionManager.stop(sessionId);
    }
}
