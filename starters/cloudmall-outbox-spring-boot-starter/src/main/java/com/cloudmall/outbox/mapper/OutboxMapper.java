package com.cloudmall.outbox.mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.mybatisplus.query.SqlLimit;
import com.cloudmall.outbox.enums.OutboxStatusEnum;
import com.cloudmall.outbox.model.entity.OutboxDO;

/**
 * 通用本地消息 Outbox Mapper。
 * <p>
 * 所有查询与状态迁移以 {@code default} 方法 + Wrapper 实现，禁止注解脚本 SQL。
 *
 * @author : Tomatos
 */
@Mapper
public interface OutboxMapper extends BaseMapper<OutboxDO> {

    /**
     * 查询到期待处理事件（按 ID 升序）。
     *
     * @param eventTypes 事件类型集合，为空表示不过滤
     * @param limit 条数上限
     * @return 待处理事件
     */
    default List<OutboxDO> listPending(Collection<String> eventTypes, int limit) {
        return selectList(
                Wrappers.<OutboxDO>lambdaQuery()
                        .eq(OutboxDO::getStatus, OutboxStatusEnum.PENDING)
                        .and(
                                wrapper -> wrapper.isNull(OutboxDO::getNextRetryTime)
                                                  .or()
                                                  .le(OutboxDO::getNextRetryTime, LocalDateTime.now())
                        )
                        .in(CollUtil.isNotEmpty(eventTypes), OutboxDO::getEventType, eventTypes)
                        .orderByAsc(OutboxDO::getId)
                        .last(SqlLimit.of(limit))
        );
    }

    /**
     * 抢占事件：PENDING 迁移为 PROCESSING。
     *
     * @param id 事件 ID
     * @return 影响行数
     */
    default int claim(Long id) {
        return update(
                Wrappers.<OutboxDO>lambdaUpdate()
                        .set(OutboxDO::getStatus, OutboxStatusEnum.PROCESSING)
                        .eq(OutboxDO::getId, id)
                        .eq(OutboxDO::getStatus, OutboxStatusEnum.PENDING)
        );
    }

    /**
     * 标记事件处理完成。
     *
     * @param id 事件 ID
     * @return 影响行数
     */
    default int markDone(Long id) {
        return update(
                Wrappers.<OutboxDO>lambdaUpdate()
                        .set(OutboxDO::getStatus, OutboxStatusEnum.DONE)
                        .set(OutboxDO::getNextRetryTime, null)
                        .eq(OutboxDO::getId, id)
        );
    }

    /**
     * 记录处理失败：递增重试次数，未达上限回到待处理并退避，达到上限转为失败。 <p>
     * {@code retry_count} 的自增必须放在最后，保证前面的条件读取到旧值。
     *
     * @param id 事件 ID
     * @param maxAttempts 最大重试次数
     * @param backoffSeconds 退避秒数
     * @return 影响行数
     */
    default int markFailed(Long id, int maxAttempts, int backoffSeconds) {
        final String nextRetryTimeSql = String.format(
                "next_retry_time = IF(retry_count + 1 >= %d, NULL, DATE_ADD(NOW(), INTERVAL %d SECOND))",
                maxAttempts,
                backoffSeconds
        );
        final String statusSql = String.format(
                "status = IF(retry_count + 1 >= %d, '%s', '%s')",
                maxAttempts,
                OutboxStatusEnum.FAILED.getCode(),
                OutboxStatusEnum.PENDING.getCode()
        );
        final String retryCountSql = "retry_count = retry_count + 1";
        return update(
                Wrappers.<OutboxDO>lambdaUpdate()
                        .setSql(nextRetryTimeSql)
                        .setSql(statusSql)
                        .setSql(retryCountSql)
                        .eq(OutboxDO::getId, id)
        );
    }

    /**
     * 删除早于指定时间且已完成的事件。
     *
     * @param before 时间阈值
     * @return 影响行数
     */
    default int deleteDoneBefore(LocalDateTime before) {
        return delete(
                Wrappers.<OutboxDO>lambdaQuery()
                        .eq(OutboxDO::getStatus, OutboxStatusEnum.DONE)
                        .lt(OutboxDO::getCreateTime, before)
        );
    }
}
