package com.cloudmall.im.infrastructure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * IM 分布式运行配置。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
@Data
@ConfigurationProperties(prefix = "cloudmall.im")
public class ImProperties {

    /** 实例 ID，为空时自动生成 */
    private String instanceId = "";

    /** 在线路由 TTL（秒） */
    private int presenceTtlSeconds = 600;

    /** 推送模式：broadcast / routed */
    private String pushMode = "broadcast";

    private Heartbeat heartbeat = new Heartbeat();
    private Mq mq = new Mq();

    @Data
    public static class Heartbeat {
        private int staleTimeoutSeconds = 90;
    }

    @Data
    public static class Mq {
        private String topic = "im-message-push";
        private String tag = "push";
        private String consumerGroup = "im-service";
    }
}
