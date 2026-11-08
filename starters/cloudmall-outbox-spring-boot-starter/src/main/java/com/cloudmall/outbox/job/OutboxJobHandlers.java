package com.cloudmall.outbox.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.cloudmall.outbox.dispatcher.OutboxDispatcher;
import com.cloudmall.outbox.properties.OutboxProperties;

/**
 * Outbox 默认调度任务
 * <p>
 * 提供发布与清理两个通用任务，各服务在 xxl-job 配置中按需注册触发。
 * 由 {@code OutboxAutoConfiguration} 条件装配：classpath 存在 xxl-job
 * 与 MQ 客户端时才生效；服务可声明同名 Bean 覆盖默认实现。
 *
 * @author : Tomatos
 */
@Slf4j
@RequiredArgsConstructor
public class OutboxJobHandlers {

    private final OutboxDispatcher outboxDispatcher;
    private final OutboxProperties properties;

    /**
     * 发布一批待发布事件，发布后输出死信告警。
     */
    @XxlJob("outboxPublish")
    public void publish() {
        int published = outboxDispatcher.publishPending(properties.getBatchSize());
        if (published > 0) {
            log.debug("Outbox 发布 {} 条消息", published);
        }
        outboxDispatcher.logDeadLetters();
    }

    /**
     * 清理已发布超过保留期的记录，死信保留供人工排查。
     *
     * @param param 任务参数（未使用）
     */
    @XxlJob("outboxCleanup")
    public void cleanup(String param) {
        outboxDispatcher.cleanup(properties.getRetentionDays());
    }
}
