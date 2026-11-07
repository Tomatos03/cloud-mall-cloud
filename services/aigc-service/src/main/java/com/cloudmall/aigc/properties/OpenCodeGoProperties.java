package com.cloudmall.aigc.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenCode Go 聊天模型配置属性。
 * <p>
 * OpenCode Go 提供 OpenAI 兼容的 Chat Completions 接口，
 * 通过替换 base URL、API Key 与模型 ID 即可复用 Spring AI 的 OpenAI 客户端。
 *
 * @author : Tomatos
 * @date : 2026/11/7
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "agent.opencode-go")
public class OpenCodeGoProperties {

    /**
     * 是否启用 OpenCode Go 作为聊天模型，未启用时回退到 OpenAI 聊天模型。
     */
    private boolean enabled;

    /**
     * OpenAI 兼容端点，结尾不带 /v1，Spring AI 会自动拼接 /v1/chat/completions。
     */
    @Builder.Default
    private String baseUrl = "https://opencode.ai/inference/go/openai";

    /**
     * OpenCode Go API Key，建议通过环境变量 OPENCODE_GO_API_KEY 注入。
     */
    private String apiKey;

    /**
     * 模型 ID。
     */
    @Builder.Default
    private String model = "deepseek-v4.1-flash";

    /**
     * OpenCode 会话标识，用于路由与提示词缓存；为空时启动时生成。
     */
    private String sessionId;
}
