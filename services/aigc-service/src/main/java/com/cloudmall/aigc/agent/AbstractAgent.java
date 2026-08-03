package com.cloudmall.aigc.agent;

import java.util.Collections;
import java.util.Map;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import reactor.core.publisher.Flux;

import com.cloudmall.aigc.manager.GeneratingSessionManager;
import com.cloudmall.aigc.model.enums.ChatEventType;
import com.cloudmall.aigc.model.req.ChatReq;
import com.cloudmall.aigc.model.resp.ChatResp;
import com.cloudmall.aigc.service.ISessionService;
import com.cloudmall.aigc.tool.ToolResultContext;
import com.cloudmall.common.context.UserContextHolder;

@Slf4j
public abstract class AbstractAgent implements Agent {
    private final GeneratingSessionManager generatingSessionManager;
    private final ChatMemoryRepository chatMemoryRepository;
    private final ISessionService sessionService;
    protected ChatClient chatClient;

    /**
     * 使用指定参数构造对象。
     *
     * @param promptLoader 提示词加载器
     * @param chatClientBuilder 聊天客户端构建器
     * @param generatingSessionManager 生成会话管理器
     * @param chatMemoryRepository 聊天记忆仓库
     * @param sessionService 会话服务
     * @param toolRegistry 工具注册表
     * @param messageChatMemoryAdvisor 聊天记忆顾问
     */
    protected AbstractAgent(
            AgentPromptLoader promptLoader,
            ChatClient.Builder chatClientBuilder,
            GeneratingSessionManager generatingSessionManager,
            ChatMemoryRepository chatMemoryRepository,
            ISessionService sessionService,
            AgentToolRegistry toolRegistry,
            MessageChatMemoryAdvisor messageChatMemoryAdvisor
    ) {
        this.generatingSessionManager = generatingSessionManager;
        this.chatMemoryRepository = chatMemoryRepository;
        this.sessionService = sessionService;
        String systemPrompt = promptLoader.load(getType());
        if (log.isDebugEnabled()) {
            log.debug("{} 初始化 ChatClient, systemPrompt:\n{}", getType().name(), systemPrompt);
        }

        promptLoader.registerListener(getType(), this::rebuildChatClient);

        Object[] tools = toolRegistry.getTools(getType());
        log.debug("{} 初始化 ChatClient, tools: {}", getType().name(), tools);

        this.chatClient = chatClientBuilder.defaultSystem(systemPrompt)
                                           .defaultAdvisors(messageChatMemoryAdvisor)
                                           .defaultTools(tools)
                                           .build();
    }

    /**
     * Nacos 提示词热更新回调 — 子类覆写，重建自己持有的 ChatClient。
     */
    protected abstract void rebuildChatClient();

    /**
     * 处理聊天请求，返回流式响应。 <p>
     * 首次聊天时自动设置会话标题，生成过程中将内容追加到 StringBuilder， 完成后保存到记忆仓
     * 库。支持工具结果上下文的追加输出。
     *
     * @param req 聊天请求，包含会话ID和用户提示词
     * @return 流式聊天响应，包含内容块和可选的工具结果
     */
    @Override
    public Flux<ChatResp> chat(ChatReq req) {
        String sessionId = req.getSessionId();
        // 只有首次聊天（标题为 null）才设置标题，后续聊天不再覆盖
        if (sessionService.getSessionTitle(sessionId) == null) {
            sessionService.updateSessionTitle(sessionId, req.getPrompt());
        }

        StringBuilder generatedContent = new StringBuilder();
        String conversationId = buildConversationId(sessionId);
        ToolResultContext resultContext = new ToolResultContext();

        log.debug(
                "{} 处理 chat 请求: sessionId={}, prompt={}",
                getType().name(),
                sessionId,
                req.getPrompt()
        );

        return getChatClient()
                .prompt(req.getPrompt())
                .advisors(
                        advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId)
                )
                .toolContext(Map.of("toolResultContext", resultContext))
                .stream()
                .content()
                .doFirst(() -> generatingSessionManager.start(sessionId))
                .doOnComplete(
                        () -> {
                            saveGeneratedMessage(conversationId, generatedContent.toString());
                            generatingSessionManager.finish(sessionId);
                        }
                )
                .doOnError(
                        e -> {
                            log.error("生成异常: sessionId={}", sessionId, e);
                            generatingSessionManager.finish(sessionId);
                        }
                )
                .doOnCancel(
                        () -> {
                            saveGeneratedMessage(conversationId, generatedContent.toString());
                            generatingSessionManager.finish(sessionId);
                        }
                )
                .takeWhile(content -> generatingSessionManager.isGenerating(sessionId))
                .map(
                        content -> {
                            generatedContent.append(content);
                            return ChatResp.builder()
                                           .data(content)
                                           .eventType(ChatEventType.MESSAGE)
                                           .build();
                        }
                )
                .concatWith(
                        Flux.defer(
                                () -> resultContext.hasResults()
                                        ? Flux.just(
                                                ChatResp.param(resultContext.getResults()),
                                                ChatResp.end()
                                        )
                                        : Flux.just(ChatResp.end())
                        )
                );
    }

    /**
     * 纯文本非流式聊天 — 等待完整响应后一次性返回 AI 回复内容。 <p>
     * 通过 {@link MessageChatMemoryAdvisor} 传递会话 ID 以维护聊天上
     * 下文， 由 Advisor 自动处理消息记忆，无需手动保存。
     *
     * @param req 聊天请求（含 sessionId 和 prompt）
     * @return AI 回复的完整文本内容
     */
    public String chatText(ChatReq req) {
        String sessionId = req.getSessionId();
        String conversationId = buildConversationId(sessionId);

        log.debug(
                "{} 处理 chatText 请求: sessionId={}, prompt={}",
                getType().name(),
                sessionId,
                req.getPrompt()
        );

        return getChatClient()
                .prompt(req.getPrompt())
                .advisors(
                        advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId)
                )
                .call()
                .content();
    }

    /**
     * 构建会话唯一标识，格式为 "{用户ID}_{会话ID}"。
     *
     * @param sessionId 会话 ID
     * @return 会话唯一标识
     */
    private String buildConversationId(String sessionId) {
        return String.format("%s_%s", UserContextHolder.getUserId(), sessionId);
    }

    /**
     * 保存 LLM 生成的回复消息到记忆仓库。
     *
     * @param conversationId 会话唯一标识
     * @param content 生成的回复内容
     */
    private void saveGeneratedMessage(String conversationId, String content) {
        if (StrUtil.isBlank(content)) {
            return;
        }
        AssistantMessage message = AssistantMessage.builder()
                                                   .content(content)
                                                   .build();
        chatMemoryRepository.saveAll(conversationId, Collections.singletonList(message));
    }
}
