package com.cloudmall.outbox.mapper;

import java.time.LocalDateTime;
import java.util.Collection;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.outbox.model.entity.OutboxDO;
import com.cloudmall.outbox.enums.OutboxPublishStatusEnum;

/**
 * 通用本地消息 Outbox Mapper
 * <p>
 * 所有服务共用同一张表，通过 app_name 隔离各自的待发布事件。
 * 由 OutboxAutoConfiguration 的 @AutoConfigurationPackage + @AutoConfigureBefore 保证被 MyBatis @Mapper 自动扫描注册。
 *
 * @author : Tomatos
 */
@Mapper
public interface OutboxMapper extends BaseMapper<OutboxDO> {

    /**
     * 查询到期待发布事件（已到重试时间，按 ID 升序）。
     *
     * @param appName 来源服务
     * @param page 分页参数
     * @return 待发布事件分页数据
     */
    default IPage<OutboxDO> listDuePending(String appName, Page<OutboxDO> page) {
        return selectPage(
                page,
                Wrappers.<OutboxDO>lambdaQuery()
                        .eq(OutboxDO::getAppName, appName)
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
     * 标记事件已发布。
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
     * 记录发布失败：重试次数、退避时间、错误信息与目标状态。
     *
     * @param id Outbox 记录 ID
     * @param retryCount 更新后重试次数
     * @param nextRetryTime 下次重试时间
     * @param lastError 最后错误信息
     * @param status 目标发布状态（PENDING/DEAD）
     * @return 影响行数
     */
    default int markFailed(
        Long id,
        Integer retryCount,
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

    /**
     * 批量标记已发布。 <p>
     * 成功路径整批一次 UPDATE，替代逐行 {@link #markPublished}。
     *
     * @param ids 记录 ID 列表
     * @return 影响行数
     */
    default int markPublishedBatch(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        return update(
                Wrappers.<OutboxDO>lambdaUpdate()
                        .set(OutboxDO::getPublishStatus, OutboxPublishStatusEnum.PUBLISHED)
                        .set(OutboxDO::getPublishTime, LocalDateTime.now())
                        .set(OutboxDO::getLastError, null)
                        .in(OutboxDO::getId, ids)
        );
    }

    /**
     * 批量记录发布失败：重试次数原地 +1、指数退避下次重试、超限进入死信。 <p>
     * 各行的退避时间与死信判定基于自身的新重试次数（SQL 表达式计算）， 与逐行
     * {@link #markFailed} 语义等价，但整批一次 UPDATE。
     *
     * @param ids 记录 ID 列表
     * @param lastError 最后错误信息
     * @param maxRetry 最大重试次数（超过进入死信）
     * @param retryBaseSeconds 初始重试间隔秒数（指数退避）
     * @return 影响行数
     */
    default int markFailedBatch(Collection<Long> ids, String lastError, int maxRetry, int retryBaseSeconds) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        final String nextRetryTimeSql = String.format(
                "next_retry_time = DATE_ADD(NOW(), INTERVAL (retry_count + 1) * %d SECOND)",
                retryBaseSeconds
        );
        final String publishStatusSql = String.format(
                "publish_status = IF(retry_count + 1 >= %d, 'DEAD', 'PENDING')",
                maxRetry
        );
        return update(
                Wrappers.<OutboxDO>lambdaUpdate()
                        .setSql("retry_count = retry_count + 1")
                        .setSql(nextRetryTimeSql)
                        .setSql(publishStatusSql)
                        .set(OutboxDO::getLastError, lastError)
                        .in(OutboxDO::getId, ids)
        );
    }

    /**
     * 统计死信数量。
     *
     * @param appName 来源服务
     * @return 死信数量
     */
    default long countDead(String appName) {
        return selectCount(
                Wrappers.<OutboxDO>lambdaQuery()
                        .eq(OutboxDO::getAppName, appName)
                        .eq(OutboxDO::getPublishStatus, OutboxPublishStatusEnum.DEAD)
        );
    }

    /**
     * 删除已发布且发布时间早于指定时间的记录。
     *
     * @param appName 来源服务
     * @param before 时间阈值
     * @return 影响行数
     */
    default int deletePublishedBefore(String appName, LocalDateTime before) {
        return delete(
                Wrappers.<OutboxDO>lambdaQuery()
                        .eq(OutboxDO::getAppName, appName)
                        .eq(OutboxDO::getPublishStatus, OutboxPublishStatusEnum.PUBLISHED)
                        .lt(OutboxDO::getPublishTime, before)
        );
    }
}
