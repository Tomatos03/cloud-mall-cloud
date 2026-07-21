package com.cloudmall.agent.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "agent")
public class AgentProperties {
    private String prompt;
    private Session session = new Session();

    @Data
    public static class Session {
        private String title;
        private String describe;
        private List<HotTopic> hotTopics = List.of();
    }

    @Data
    public static class HotTopic {
        private String title;
        private String describe;
    }
}
