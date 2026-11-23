package com.cloudmall.shared.job;

import java.util.List;
import java.util.Set;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cloudmall.outbox.model.OutboxRecord;
import com.cloudmall.outbox.repository.OutboxRepository;
import com.cloudmall.shared.api.constant.FileEventConstants;
import com.cloudmall.shared.service.IFileService;

/**
 * 文件事件消费任务。
 * <p>
 * 扫描 Outbox 中的文件事件并应用到文件台账。事件与业务写入在同一事务内产生，
 * 因此不会出现"业务回滚但文件已绑定"的泄漏。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileEventApplyJobHandler {

    /** 本服务关心的文件事件类型 */
    private static final Set<String> FILE_EVENT_TYPES = Set.of(
            FileEventConstants.BIND,
            FileEventConstants.RELEASE
    );

    private final OutboxRepository outboxRepository;
    private final IFileService fileService;

    /**
     * 消费待处理的文件事件。
     */
    @XxlJob("fileEventApply")
    public void apply() {
        outboxRepository.consume(FILE_EVENT_TYPES, this::applyOne);
    }

    /**
     * 清理超过保留期的已完成事件。
     */
    @XxlJob("fileEventCleanup")
    public void cleanup() {
        int removed = outboxRepository.cleanup();
        if (removed > 0) {
            log.info("文件事件 Outbox 清理完成，删除 {} 条", removed);
        }
    }

    /**
     * 将单条事件应用到文件台账。
     *
     * @param record 事件记录
     */
    private void applyOne(OutboxRecord record) {
        switch (record.eventType()) {
            case FileEventConstants.BIND -> fileService.bind(
                    Long.valueOf(record.bizId()),
                    null,
                    List.of(record.payload())
            );
            case FileEventConstants.RELEASE -> fileService.release(List.of(record.payload()));
            default -> throw new IllegalStateException("未知文件事件类型: " + record.eventType());
        }
    }
}
