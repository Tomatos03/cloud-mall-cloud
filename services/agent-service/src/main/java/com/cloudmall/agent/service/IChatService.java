package com.cloudmall.agent.service;

import com.cloudmall.agent.model.req.ChatReq;
import com.cloudmall.agent.model.resp.ChatResp;
import reactor.core.publisher.Flux;

/**
 * AI 智能客服接口
 *
 * @author : Tomatos
 * @date : 2026/7/21
 */
public interface IChatService {

    /**
     * 流式聊天 — 逐 token 返回 AI 回复内容
     *
     * @param req 聊天请求（含 sessionId 和 prompt）
     * @return SSE 事件流，包含 DATA 和 END 两种事件
     */
    Flux<ChatResp> chat(ChatReq req);

    /**
     * 停止指定会话的 AI 生成
     *
     * @param sessionId 会话 ID
     */
    void stop(String sessionId);
}
