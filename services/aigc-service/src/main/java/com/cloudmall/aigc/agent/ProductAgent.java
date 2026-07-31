package com.cloudmall.aigc.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.stereotype.Service;

import com.cloudmall.aigc.manager.GeneratingSessionManager;
import com.cloudmall.aigc.service.ISessionService;

/**
 * 商品咨询智能体 — 商品搜索、详情查询、价格库存、分类浏览和商品推荐。
 */
@Slf4j
@Service
public class ProductAgent extends AbstractAgent {

    /**
     * 构造商品咨询智能体实例。
     *
     * @param promptLoader 提示词加载器
     * @param chatClientBuilder 聊天客户端构建器
     * @param generatingSessionManager 生成会话管理器
     * @param chatMemoryRepository 聊天记忆仓库
     * @param sessionService 会话服务
     * @param toolRegistry 工具注册表
     * @param messageChatMemoryAdvisor 聊天记忆顾问
     */
    protected ProductAgent(
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
     * @return {@link AgentType#PRODUCT}
     */
    @Override
    public AgentType getType() {
        return AgentType.PRODUCT;
    }

    /**
     * Nacos 提示词热更新时重建 ChatClient。
     */
    @Override
    protected void rebuildChatClient() {
        log.info("ProductAgent ChatClient 已重建（热更新）");
    }
}
