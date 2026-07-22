package com.cloudmall.agent.config;

import cn.hutool.core.collection.CollectionUtil;
import com.cloudmall.agent.memory.CacheChatMemoryRepository;
import com.cloudmall.agent.properties.AgentProperties;
import com.cloudmall.agent.tool.AgentTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * Agent 服务配置类
 * <p>
 * 配置 ChatClient、Advisors、Tools 等核心 Bean
 *
 * @author Tomatos
 * @date 2026/7/21
 */
@Configuration
public class AgentConfig {

    /**
     * 创建 ChatClient Bean
     *
     * @param builder        ChatClient 构建器
     * @param advisors       所有 Advisor 列表
     * @param tools          所有 AgentTool 列表
     * @param agentProperties Agent 配置属性
     * @return ChatClient 实例
     */
    @Bean
    public ChatClient chatClient(
            ChatClient.Builder builder,
            List<Advisor> advisors,
            List<AgentTool> tools,
            AgentProperties agentProperties
    ) {
        if (CollectionUtil.isNotEmpty(advisors)) {
            builder.defaultAdvisors(advisors);
        }
        if (CollectionUtil.isNotEmpty(tools)) {
            builder.defaultTools(tools.toArray());
        }
        return builder.defaultSystem(agentProperties.getPrompt())
                      .build();
    }

    /**
     * 日志 Advisor
     *
     * @return SimpleLoggerAdvisor 实例
     */
    @Bean
    public Advisor simpleAdvisor() {
        return new SimpleLoggerAdvisor();
    }

    /**
     * 基于 Redis 的聊天记忆仓储
     *
     * @param stringRedisTemplate Redis 模板
     * @return ChatMemoryRepository 实例
     */
    @Bean
    public ChatMemoryRepository redisChatMemoryRepository(StringRedisTemplate stringRedisTemplate) {
        return new CacheChatMemoryRepository(stringRedisTemplate);
    }

    /**
     * 消息记忆 Advisor — 在聊天过程中维护上下文
     *
     * @param chatMemory 聊天记忆管理器实例
     * @return MessageChatMemoryAdvisor 实例
     */
    @Bean
    public Advisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor
                .builder(chatMemory)
                .build();
    }

    /**
     * 问答 Advisor — 基于向量检索实现 RAG
     *
     * @param vectorStore      向量存储
     * @param agentProperties  配置属性
     * @return QuestionAnswerAdvisor 实例
     */
    @Bean
    @ConditionalOnBean(VectorStore.class)
    @ConditionalOnProperty(prefix = "agent.rag", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Advisor questionAnswerAdvisor(VectorStore vectorStore, AgentProperties agentProperties) {
        AgentProperties.Rag rag = agentProperties.getRag();
        SearchRequest searchRequest = SearchRequest
                .builder()
                .similarityThreshold(rag.getSimilarityThreshold())
                .topK(rag.getTopK())
                .build();
        QuestionAnswerAdvisor advisor = QuestionAnswerAdvisor.builder(vectorStore)
                                                             .searchRequest(searchRequest)
                                                             .build();
        return advisor;
    }

    /**
     * 聊天记忆管理器
     *
     * @param chatMemoryRepository 聊天记忆仓储
     * @return ChatMemory 实例
     */
    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                                      .chatMemoryRepository(chatMemoryRepository)
                                      .maxMessages(30)
                                      .build();
    }
}