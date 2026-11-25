package com.cloudmall.outbox.relay.properties;

import java.util.List;
import java.util.Map;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Outbox Relay 运行参数。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
@Data
@ConfigurationProperties(prefix = "cloudmall.outbox-relay")
public class OutboxRelayProperties {

    /** 是否启用 Relays */
    private boolean enabled = true;

    /** 只处理本服务的事件 */
    private String appName = "";

    /** 需要投递到 MQ 的事件类型 */
    private List<String> eventTypes = List.of();

    /** 默认 RocketMQ 投递映射 */
    private Map<String, Mapping> mappings = Map.of();

    @Data
    public static class Mapping {
        private String topic;
        private String tag;
        private boolean orderly;
        private String hashKeyField;
    }
}
