package com.cloudmall.aigc.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/**
 * Agent 服务配置属性
 *
 * @author Tomatos
 * @date 2026/7/21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "agent")
public class AgentProperties {
    private String prompt;
    private Session session = new Session();
    private Rag rag = new Rag();

    /** ========== 多智能体提示词配置 ========== */

    /** 所有智能体共享的全局 prompt，拼接在每个智能体专属 prompt 之前 */
    private String globalPrompt;

    /** 默认 Nacos group */
    private String promptGroup = "DEFAULT_GROUP";

    /** 默认 Nacos 读取超时（毫秒） */
    private int readTimeout = 3000;

    /** 各智能体的 Nacos 提示词文件配置，key 为 AgentType 小写名 */
    private Map<String, PromptConfig> prompts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromptConfig {
        /** Nacos dataId */
        private String dataId;
        /** Nacos group，为空则继承 promptGroup */
        private String group;
        /** 读取超时（毫秒），为空则继承 readTimeout */
        private Integer readTimeout;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Session {
        private String title;
        private String describe;
        private List<HotTopic> hotTopics = List.of();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotTopic {
        private String title;
        private String describe;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rag {
        /** 是否启用 RAG */
        private boolean enabled = true;
        /** 相似度阈值 */
        private double similarityThreshold = 0.5;
        /** 返回 top K 个文档 */
        private int topK = 3;
    }
}
