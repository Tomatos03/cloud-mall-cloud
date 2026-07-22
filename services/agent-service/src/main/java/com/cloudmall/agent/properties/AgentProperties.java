package com.cloudmall.agent.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

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
