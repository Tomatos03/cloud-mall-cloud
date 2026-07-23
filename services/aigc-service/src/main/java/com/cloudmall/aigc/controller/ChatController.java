package com.cloudmall.aigc.controller;

import com.cloudmall.aigc.model.req.ChatReq;
import com.cloudmall.aigc.model.resp.ChatResp;
import com.cloudmall.aigc.service.IChatService;
import com.cloudmall.common.entity.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI 智能客服接口 — 流式聊天、停止生成
 *
 * @author : Tomatos
 * @date : 2026/7/21
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final IChatService chatService;

    /**
     * 流式聊天 — SSE 方式逐 token 返回 AI 回复
     *
     * @param req 聊天请求（含 sessionId 和 prompt）
     * @return SSE 事件流
     */
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResp> chat(@Valid @RequestBody ChatReq req) {
        return chatService.chat(req);
    }

    /**
     * 停止指定会话的 AI 生成
     *
     * @param sessionId 会话 ID
     */
    @PostMapping("/{sessionId}/stop")
    public Result<Void> stop(@PathVariable String sessionId) {
        chatService.stop(sessionId);
        return Result.success();
    }
}
