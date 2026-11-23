package com.cloudmall.outbox.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Outbox 运行参数。
 * <p>
 * 前缀 cloudmall.outbox，表结构固定，仅暴露消费侧运行参数。
 *
 * @author : Tomatos
 */
@Data
@ConfigurationProperties(prefix = "cloudmall.outbox")
public class OutboxProperties {

    /** 单批扫描条数 */
    private int batchSize = 100;

    /** 最大重试次数，达到后转为失败 */
    private int maxAttempts = 10;

    /** 重试退避间隔秒数 */
    private int backoffSeconds = 30;

    /** 已完成事件保留天数 */
    private int retentionDays = 7;
}
