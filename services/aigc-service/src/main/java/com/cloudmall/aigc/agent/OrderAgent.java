package com.cloudmall.aigc.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.stereotype.Service;

import com.cloudmall.aigc.manager.GeneratingSessionManager;
import com.cloudmall.aigc.service.ISessionService;

/**
 * 订单智能体，负责订单查询、下单、物流跟踪、购物车管理。
 */
@Slf4j
@Service
public class OrderAgent extends AbstractAgent {

    /**
     * 构造订单智能体实例。
     *
     * @param promptLoader 提示词加载器
     * @param chatClientBuilder 聊天客户端构建器
     * @param generatingSessionManager 生成会话管理器
     * @param chatMemoryRepository 聊天记忆仓库
     * @param sessionService 会话服务
     * @param toolRegistry 工具注册表
     * @param messageChatMemoryAdvisor 聊天记忆顾问
     */
    protected OrderAgent(
        AgentPromptLoader promptLoader,
        ChatClient.Builder chatClientBuilder,
        GeneratingSessionManager generatingSessionManager,
        ChatMemoryRepository chatMemoryRepository,
        ISessionService sessionService,
        AgentToolRegistry toolRegistry,
        MessageChatMemoryAdvisor messageChatMemoryAdvisor
    ) {
        super(
                promptLoader,
                chatClientBuilder,
                generatingSessionManager,
                chatMemoryRepository,
                sessionService,
                toolRegistry,
                messageChatMemoryAdvisor
        );
    }

    /**
     * 获取该智能体的 ChatClient 实例。
     *
     * @return ChatClient 实例
     */
    @Override
    public ChatClient getChatClient() {
        return chatClient;
    }

    /**
     * 获取智能体类型。
     *
     * @return {@link AgentType#ORDER}
     */
    @Override
    public AgentType getType() {
        return AgentType.ORDER;
    }

    /**
     * Nacos 提示词热更新时重建 ChatClient。
     */
    @Override
    protected void rebuildChatClient() {
        log.info("OrderAgent ChatClient 已重建（热更新）");
    }
}
