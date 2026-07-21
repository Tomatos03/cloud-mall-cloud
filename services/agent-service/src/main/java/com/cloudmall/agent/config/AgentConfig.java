package com.cloudmall.agent.config;

import cn.hutool.core.collection.CollectionUtil;
import com.cloudmall.agent.memory.CacheChatMemoryRepository;
import com.cloudmall.agent.properties.AgentProperties;
import com.cloudmall.agent.tool.AgentTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@Configuration
public class AgentConfig {
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
            builder.defaultTools(tools);
        }
        return builder.defaultSystem(agentProperties.getPrompt())
                      .build();
    }

    @Bean
    public Advisor simpleAdvisor() {
        return new SimpleLoggerAdvisor();
    }

    @Bean
    public ChatMemoryRepository redisChatMemoryRepository(StringRedisTemplate stringRedisTemplate) {
        return new CacheChatMemoryRepository(stringRedisTemplate);
    }

    /*
     *
     * @param chatMemory 聊天记忆管理器实例
     * @return MessageChatMemoryAdvisor 实例，用于在聊天过程中维护上下文
     */
    @Bean
    public Advisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor
                .builder(chatMemory)
                .build();
    }

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                                      .chatMemoryRepository(chatMemoryRepository)
                                      .maxMessages(30)
                                      .build();
    }
}