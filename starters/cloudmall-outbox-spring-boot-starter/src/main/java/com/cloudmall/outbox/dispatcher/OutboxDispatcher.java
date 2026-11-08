package com.cloudmall.outbox.dispatcher;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.cloudmall.outbox.mapper.OutboxMapper;
import com.cloudmall.outbox.model.entity.OutboxDO;
import com.cloudmall.outbox.properties.OutboxProperties;
import com.cloudmall.outbox.publisher.IOutboxPublisher;

/**
 * Outbox 通用分发器
 * <p>
 * 扫描本服务待发布事件，把 payload JSON 原样转发到行内 topic 与 tag；
 * 同 (topic, tag) 的事件分组后批量发送，单批超过字节上限自动切批；
 * 发送失败按指数退避重试，超限进入死信。事件类型知识由消费端持有，
 * 本分发器不感知任何事件结构。
 * 单次触发仅在一个执行器实例执行（xxl-job 保证），无需行锁。
 *
 * @author : Tomatos
 */
@Slf4j
@RequiredArgsConstructor
public class OutboxDispatcher {

    /** 最大重试次数，超过进入死信 */
    private static final int MAX_RETRY = 10;

    /** 初始重试间隔秒数（指数退避） */
    private static final int RETRY_BASE_SECONDS = 30;

    private final OutboxMapper outboxMapper;
    private final IOutboxPublisher publisher;

    /** 来源服务（spring.application.name） */
    private final String appName;

    /** Outbox 配置（批次字节上限等） */
    private final OutboxProperties properties;

    /**
     * 发布一批待发布事件：按 (topic, tag) 分组批量发送。
     *
     * @param batchSize 扫描条数上限
     * @return 本次尝试发布条数
     */
    public int publishPending(int batchSize) {
        Page<OutboxDO> page = new Page<>(1, batchSize);
        List<OutboxDO> pending = outboxMapper.listDuePending(appName, page).getRecords();
        if (CollUtil.isEmpty(pending)) {
            return 0;
        }
        Map<String, List<OutboxDO>> groups = pending.stream()
                                                    .collect(
                                                            Collectors.groupingBy(OutboxDispatcher::groupKey)
                                                    );
        for (List<OutboxDO> group : groups.values()) {
            publishGroup(group);
        }
        return pending.size();
    }

    /**
     * 死信数量超过零时输出告警日志。
     */
    public void logDeadLetters() {
        long dead = outboxMapper.countDead(appName);
        if (dead > 0) {
            log.warn("Outbox 死信 appName={} count={}", appName, dead);
        }
    }

    /**
     * 清理已发布超过保留期的记录。
     *
     * @param retentionDays 保留天数
     * @return 删除条数
     */
    public int cleanup(int retentionDays) {
        LocalDateTime before = LocalDateTime.now().minusDays(retentionDays);
        return outboxMapper.deletePublishedBefore(appName, before);
    }

    /**
     * 发布同 (topic, tag) 的一组事件：按字节上限切批后批量发送。 发送成功整批标记已发布；失
     * 败整批进入重试/退避/死信状态。
     *
     * @param group 同组事件
     */
    private void publishGroup(List<OutboxDO> group) {
        OutboxDO first = group.get(0);
        String topic = first.getTopic();
        String tag = first.getEventType();
        for (List<OutboxDO> batch : splitByMaxBytes(group)) {
            List<Long> ids = batch.stream()
                                  .map(OutboxDO::getId)
                                  .toList();
            try {
                List<byte[]> payloads = batch
                        .stream()
                        .map(outbox -> outbox.getPayload().getBytes(StandardCharsets.UTF_8))
                        .toList();
                publisher.publishBatch(topic, tag, payloads);
                outboxMapper.markPublishedBatch(ids);
            } catch (Exception e) {
                log.warn("Outbox 批量发布失败 topic={} tag={} count={}", topic, tag, batch.size(), e);
                outboxMapper.markFailedBatch(ids, e.getMessage(), MAX_RETRY, RETRY_BASE_SECONDS);
            }
        }
    }

    /**
     * 按单批字节上限切分事件列表（贪心累计，超限即切）。
     *
     * @param group 同组事件
     * @return 切分后的批次列表
     */
    private List<List<OutboxDO>> splitByMaxBytes(List<OutboxDO> group) {
        List<List<OutboxDO>> batches = new ArrayList<>();
        List<OutboxDO> current = new ArrayList<>();
        int currentBytes = 0;
        for (OutboxDO outbox : group) {
            int size = outbox.getPayload().getBytes(StandardCharsets.UTF_8).length;
            if (!current.isEmpty() && currentBytes + size > properties.getBatchMaxBytes()) {
                batches.add(current);
                current = new ArrayList<>();
                currentBytes = 0;
            }
            current.add(outbox);
            currentBytes += size;
        }
        if (!current.isEmpty()) {
            batches.add(current);
        }
        return batches;
    }

    /**
     * 分组键：topic 与 tag 组合。
     *
     * @param outbox 事件
     * @return 分组键
     */
    private static String groupKey(OutboxDO outbox) {
        return outbox.getTopic() + "|" + outbox.getEventType();
    }
}
