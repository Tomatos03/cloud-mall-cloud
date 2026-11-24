package com.cloudmall.outbox.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import cn.hutool.core.collection.CollStreamUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionTemplate;

import com.cloudmall.outbox.mapper.OutboxMapper;
import com.cloudmall.outbox.model.OutboxRecord;
import com.cloudmall.outbox.properties.OutboxProperties;

/**
 * Outbox 通用消费操作。
 * <p>
 * 提供扫描、抢占、标记完成、失败退避与清理；具体业务处理由调用方以
 * {@code Consumer} 传入。每个事件在其独立事务内完成"处理 + 标记完成"。
 *
 * @author : Tomatos
 */
@Slf4j
@RequiredArgsConstructor
public class OutboxRepository {

    private final OutboxMapper outboxMapper;
    private final TransactionTemplate transactionTemplate;
    private final OutboxProperties outboxProperties;

    /**
     * 查询到期待处理事件（不按类型过滤）。
     *
     * @param limit 条数上限
     * @return 待处理事件
     */
    public List<OutboxRecord> listPending(int limit) {
        return listPending(null, limit);
    }

    /**
     * 查询到期待处理事件。
     *
     * @param eventTypes 事件类型集合，为空表示不过滤
     * @param limit 条数上限
     * @return 待处理事件
     */
    public List<OutboxRecord> listPending(Collection<String> eventTypes, int limit) {
        return CollStreamUtil.toList(outboxMapper.listPending(eventTypes, limit), OutboxRecord::from);
    }

    /**
     * 抢占事件：PENDING 迁移为 PROCESSING。
     *
     * @param id 事件 ID
     * @return true 表示抢占成功
     */
    public boolean claim(Long id) {
        return outboxMapper.claim(id) == 1;
    }

    /**
     * 标记事件处理完成。
     *
     * @param id 事件 ID
     * @return 影响行数
     */
    public int markDone(Long id) {
        return outboxMapper.markDone(id);
    }

    /**
     * 记录处理失败：递增重试次数，未达上限回到待处理并退避，达到上限转为失败。
     *
     * @param id 事件 ID
     * @return 影响行数
     */
    public int markFailed(Long id) {
        return outboxMapper.markFailed(
                id,
                outboxProperties.getMaxAttempts(),
                outboxProperties.getBackoffSeconds()
        );
    }

    /**
     * 驱动一批事件：扫描 → 抢占 → 处理 → 标记完成；失败按退避重试。
     *
     * @param eventTypes 事件类型集合，为空表示不过滤
     * @param handler 业务处理逻辑
     */
    public void consume(Collection<String> eventTypes, Consumer<OutboxRecord> handler) {
        for (OutboxRecord record : listPending(eventTypes, outboxProperties.getBatchSize())) {
            if (!claim(record.id())) {
                continue;
            }
            try {
                transactionTemplate.executeWithoutResult(
                        status -> {
                            handler.accept(record);
                            markDone(record.id());
                        }
                );
            } catch (RuntimeException e) {
                log.warn("Outbox 事件处理失败 id={} eventType={}", record.id(), record.eventType(), e);
                markFailed(record.id());
            }
        }
    }

    /**
     * 删除超过保留期的已完成事件。
     *
     * @return 删除条数
     */
    public int cleanup() {
        return outboxMapper.deleteDoneBefore(
                LocalDateTime.now().minusDays(outboxProperties.getRetentionDays())
        );
    }
}
