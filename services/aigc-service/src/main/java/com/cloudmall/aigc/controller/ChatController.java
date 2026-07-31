package com.cloudmall.aigc.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import com.cloudmall.aigc.model.req.ChatReq;
import com.cloudmall.aigc.model.resp.ChatResp;
import com.cloudmall.aigc.model.resp.ChatTemplate;
import com.cloudmall.aigc.service.IChatService;
import com.cloudmall.common.model.Result;

/**
 * AI 智能客服接口 — 流式聊天、纯文本聊天、模板查询、停止生成
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
     * 纯文本非流式聊天 — 等待完整响应后一次性返回
     *
     * @param req 聊天请求（含 sessionId 和 prompt）
     * @return AI 回复的完整文本内容
     */
    @PostMapping("/text")
    public Result<String> chatText(@Valid @RequestBody ChatReq req) {
        return Result.success(chatService.chatText(req));
    }

    /**
     * 获取聊天回复占位符模板列表
     * <p>
     * 返回润色、续写、帮写等写作辅助模式的 prompt 模板，
     * 前端展示给用户选择，用户输入内容替换 {userInput} 占位符后提交。
     *
     * @return 模板列表
     */
    @GetMapping("/templates")
    public Result<List<ChatTemplate>> getTemplates() {
        return Result.success(chatService.getTemplates());
    }

    /**
     * 停止指定会话的 AI 生成。
     *
     * @param sessionId 会话 ID
     * @return 处理结果
     */
    @PostMapping("/{sessionId}/stop")
    public Result<Void> stop(@PathVariable String sessionId) {
        chatService.stop(sessionId);
        return Result.success();
    }
}
