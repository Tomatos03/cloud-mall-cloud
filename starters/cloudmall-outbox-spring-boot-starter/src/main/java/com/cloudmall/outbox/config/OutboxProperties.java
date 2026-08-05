package com.cloudmall.outbox.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Outbox 配置属性
 * <p>
 * 前缀 cloudmall.outbox，默认值供全部服务共享，服务级可覆盖。
 *
 * @author : Tomatos
 */
@Data
@ConfigurationProperties(prefix = "cloudmall.outbox")
public class OutboxProperties {

    /** 单批发布上限（条数） */
    private int batchSize = 100;

    /** 单批最大字节数，超过按此切批（RocketMQ 批量上限建议 1MB） */
    private int batchMaxBytes = 1024 * 1024;

    /** 已发布记录保留天数 */
    private int retentionDays = 7;
}
