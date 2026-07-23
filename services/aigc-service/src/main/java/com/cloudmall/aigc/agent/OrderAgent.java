package com.cloudmall.aigc.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
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

    protected OrderAgent(
            AgentPromptLoader promptLoader,
            ChatClient.Builder chatClientBuilder,
            GeneratingSessionManager generatingSessionManager,
            ChatMemoryRepository chatMemoryRepository,
            ISessionService sessionService,
            AgentToolRegistry toolRegistry
    ) {
        super(promptLoader, chatClientBuilder, generatingSessionManager, chatMemoryRepository, sessionService, toolRegistry);
    }

    @Override
    public ChatClient getChatClient() {
        return chatClient;
    }

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
