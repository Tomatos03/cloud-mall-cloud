package com.cloudmall.auth.mapper;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.auth.entity.OutboxDO;
import com.cloudmall.auth.enums.OutboxPublishStatusEnum;

/**
 * 认证服务本地 Outbox Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface OutboxMapper extends BaseMapper<OutboxDO> {

    /**
     * 查询到期待发布事件（已到重试时间，按 ID 升序，供定时任务扫描）。 <p>
     * 仅包含 PENDING 且未设置下次重试时间或已到下次重试时间的事件。
     *
     * @param page 分页参数
     * @return 待发布事件分页数据
     */
    default IPage<OutboxDO> selectDuePending(Page<OutboxDO> page) {
        return selectPage(
                page,
                Wrappers.<OutboxDO>lambdaQuery()
                        .eq(OutboxDO::getPublishStatus, OutboxPublishStatusEnum.PENDING)
                        .and(
                                wrapper -> wrapper.isNull(OutboxDO::getNextRetryTime)
                                                  .or()
                                                  .le(OutboxDO::getNextRetryTime, LocalDateTime.now())
                        )
                        .orderByAsc(OutboxDO::getId)
        );
    }

    /**
     * 标记事件发布成功：置为 PUBLISHED，记录发布时间并清空错误信息。
     *
     * @param id Outbox 记录 ID
     * @return 影响行数
     */
    default int markPublished(Long id) {
        return update(
                Wrappers.<OutboxDO>lambdaUpdate()
                        .set(OutboxDO::getPublishStatus, OutboxPublishStatusEnum.PUBLISHED)
                        .set(OutboxDO::getPublishTime, LocalDateTime.now())
                        .set(OutboxDO::getLastError, null)
                        .eq(OutboxDO::getId, id)
        );
    }

    /**
     * 记录发布失败：递增重试次数、退避下次重试时间并记录错误信息。
     *
     * @param id Outbox 记录 ID
     * @param retryCount 失败后的重试次数
     * @param nextRetryTime 下次重试时间
     * @param lastError 最后错误信息
     * @param status 目标发布状态（继续 PENDING 或进入 DEAD）
     * @return 影响行数
     */
    default int recordFailure(
            Long id,
            int retryCount,
            LocalDateTime nextRetryTime,
            String lastError,
            OutboxPublishStatusEnum status
    ) {
        return update(
                Wrappers.<OutboxDO>lambdaUpdate()
                        .set(OutboxDO::getRetryCount, retryCount)
                        .set(OutboxDO::getNextRetryTime, nextRetryTime)
                        .set(OutboxDO::getLastError, lastError)
                        .set(OutboxDO::getPublishStatus, status)
                        .eq(OutboxDO::getId, id)
        );
    }
}
