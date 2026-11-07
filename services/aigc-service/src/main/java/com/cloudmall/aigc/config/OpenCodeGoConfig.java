package com.cloudmall.aigc.config;

import java.util.UUID;

import cn.hutool.core.util.StrUtil;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import com.cloudmall.aigc.constant.OpenCodeGoConstants;
import com.cloudmall.aigc.properties.OpenCodeGoProperties;

/**
 * OpenCode Go 聊天模型配置。
 * <p>
 * OpenCode Go 提供 OpenAI 兼容的 Chat Completions 接口，因此复用 Spring AI 的
 * {@link OpenAiApi} 与 {@link OpenAiChatModel}，只替换 base URL、API Key 与模型 ID。
 * 该模型标记为 {@link Primary}，各 Agent 的 {@code ChatClient} 会优先使用它；
 * OpenAI 连接仍保留给 embedding、TTS 等其它能力。
 *
 * @author : Tomatos
 * @date : 2026/11/7
 */
@Configuration
@Conditional(OpenCodeGoCondition.class)
public class OpenCodeGoConfig {

    /**
     * 构建 OpenCode Go 聊天模型。
     *
     * @param properties OpenCode Go 配置属性
     * @param restClientBuilderProvider Spring Boot 管理的
     *                                  RestClient 构建器
     * @param webClientBuilderProvider Spring Boot 管理的
     *                                 WebClient 构建器
     * @return ChatModel 实例
     */
    @Bean
    @Primary
    public ChatModel openCodeGoChatModel(
        OpenCodeGoProperties properties,
        ObjectProvider<RestClient.Builder> restClientBuilderProvider,
        ObjectProvider<WebClient.Builder> webClientBuilderProvider
    ) {
        Assert.hasText(properties.getApiKey(), "agent.opencode-go.api-key 未配置，请设置 OPENCODE_GO_API_KEY");

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(OpenCodeGoConstants.SESSION_HEADER, resolveSessionId(properties));
        headers.add(OpenCodeGoConstants.ACCEPT_ENCODING_HEADER, OpenCodeGoConstants.ENCODING_IDENTITY);

        OpenAiApi openAiApi = OpenAiApi
                .builder()
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.getApiKey())
                .headers(headers)
                .restClientBuilder(restClientBuilderProvider.getIfAvailable(RestClient::builder))
                .webClientBuilder(webClientBuilderProvider.getIfAvailable(WebClient::builder))
                .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                                                     .model(properties.getModel())
                                                     .build();

        return OpenAiChatModel.builder()
                              .openAiApi(openAiApi)
                              .defaultOptions(options)
                              .build();
    }

    /**
     * 解析 OpenCode 会话标识，未显式配置时生成进程级稳定标识。
     *
     * @param properties OpenCode Go 配置属性
     * @return 会话标识
     */
    private String resolveSessionId(OpenCodeGoProperties properties) {
        return StrUtil.isBlank(properties.getSessionId())
                ? UUID.randomUUID().toString()
                : properties.getSessionId();
    }
}
