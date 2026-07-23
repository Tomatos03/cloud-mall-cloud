package com.cloudmall.aigc.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.stereotype.Service;

import com.cloudmall.aigc.manager.GeneratingSessionManager;
import com.cloudmall.aigc.service.ISessionService;

/**
 * 商品咨询智能体 — 商品搜索、详情查询、价格库存、分类浏览和商品推荐。
 */
@Slf4j
@Service
public class GoodsAgent extends AbstractAgent {

    /**
     * 构造商品咨询智能体实例。
     */
    protected GoodsAgent(
            AgentPromptLoader promptLoader,
            ChatClient.Builder chatClientBuilder,
            GeneratingSessionManager generatingSessionManager,
            ChatMemoryRepository chatMemoryRepository,
            ISessionService sessionService,
            AgentToolRegistry toolRegistry
    ) {
        super(promptLoader, chatClientBuilder, generatingSessionManager, chatMemoryRepository, sessionService, toolRegistry);
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
     * @return {@link AgentType#GOODS}
     */
    @Override
    public AgentType getType() {
        return AgentType.GOODS;
    }

    /**
     * Nacos 提示词热更新时重建 ChatClient。
     */
    @Override
    protected void rebuildChatClient() {
        log.info("GoodsAgent ChatClient 已重建（热更新）");
    }
}
