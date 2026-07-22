package com.cloudmall.agent.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cloudmall.agent.entity.ChatSessionDO;
import com.cloudmall.agent.manager.GeneratingSessionManager;
import com.cloudmall.agent.mapper.ChatSessionMapper;
import com.cloudmall.agent.model.enums.ChatEventType;
import com.cloudmall.agent.model.req.ChatReq;
import com.cloudmall.agent.model.resp.ChatResp;
import com.cloudmall.agent.service.IChatService;
import com.cloudmall.agent.tool.ToolResultContext;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.context.UserContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.Map;

/**
 * AI 智能客服实现 — 流式聊天、停止生成
 *
 * @author : Tomatos
 * @date : 2026/7/21
 */
@Service
@RequiredArgsConstructor
public class ChatService implements IChatService {

    private final ChatClient chatClient;
    private final ChatSessionMapper chatSessionMapper;
    private final ChatMemoryRepository chatMemoryRepository;
    private final GeneratingSessionManager generatingSessionManager;

    /**
     * 流式聊天 — 逐 token 返回 AI 回复
     * <p>
     * 每收到一个 token 先检查 Redis 中对应会话是否仍在生成状态，
     * 用户可通过 stop() 从任意实例打断。流结束后自动追加 PARAM 事件（包含工具调用结果），最后追加 END 标记事件。
     *
     * @param req 聊天请求（含 sessionId 和 prompt）
     * @return SSE 事件流，包含 MESSAGE、PARAM（可选）和 END 三种事件
     */
    @Override
    public Flux<ChatResp> chat(ChatReq req) {
        String sessionId = req.getSessionId();
        validateSession(sessionId);

        StringBuilder generatedContent = new StringBuilder();
        String conversationId = getConversationId(sessionId);
        ToolResultContext resultContext = new ToolResultContext();

        return chatClient.prompt(req.getPrompt())
                         .advisors(advisorSpec ->
                                 advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId)
                         )
                         .toolContext(Map.of("toolResultContext", resultContext))
                         .stream()
                         .content()
                         .doFirst(() -> generatingSessionManager.start(sessionId))
                         .doOnComplete(() -> generatingSessionManager.finish(sessionId))
                         .doOnError(e -> generatingSessionManager.finish(sessionId))
                         .doOnCancel(() -> {
                             saveGeneratedMessage(conversationId, generatedContent.toString());
                             generatingSessionManager.finish(sessionId);
                         })
                         .takeWhile(content -> generatingSessionManager.isGenerating(sessionId))
                         .map(content -> {
                             generatedContent.append(content);
                             return ChatResp.builder()
                                            .data(content)
                                            .eventType(ChatEventType.MESSAGE)
                                            .build();
                         })
                         .concatWith(Flux.defer(() -> resultContext.hasResults()
                                 ? Flux.just(ChatResp.param(resultContext.getResults()), ChatResp.end())
                                 : Flux.just(ChatResp.end())
                         ));
    }


    /**
     * 停止指定会话的 AI 生成
     *
     * @param sessionId 会话 ID
     */
    @Override
    public void stop(String sessionId) {
        generatingSessionManager.stop(sessionId);
    }

    private void saveGeneratedMessage(String conversationId, String content) {
        AssistantMessage assistantMessage = AssistantMessage.builder()
                                                            .content(content)
                                                            .build();
        chatMemoryRepository.saveAll(conversationId, Collections.singletonList(assistantMessage));
    }

    /**
     * 校验会话是否存在且属于当前用户
     *
     * @param sessionId 会话 ID
     * @throws BizException 会话不存在或不属于当前用户时抛出 SESSION_NOT_FOUND
     */
    private void validateSession(String sessionId) {
        ChatSessionDO chatSession = chatSessionMapper.selectOne(
                Wrappers.<ChatSessionDO>lambdaQuery()
                        .eq(ChatSessionDO::getSessionId, sessionId)
                        .eq(ChatSessionDO::getUserId, UserContextHolder.getUserId())
        );
        AssertUtils.notNull(chatSession, BizErrorCode.SESSION_NOT_FOUND);
    }

    /**
     * 生成会话在 AI ChatMemory 中的全局唯一标识
     *
     * @param sessionId 会话 ID
     * @return 格式为 {userId}_{sessionId} 的字符串
     */
    private String getConversationId(String sessionId) {
        return String.format("%s_%s", UserContextHolder.getUserId(), sessionId);
    }
}
