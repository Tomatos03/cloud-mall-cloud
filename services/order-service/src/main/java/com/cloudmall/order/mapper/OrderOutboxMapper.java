package com.cloudmall.order.mapper;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.order.entity.OrderOutboxDO;
import com.cloudmall.order.enums.OutboxPublishStatusEnum;

/**
 * 订单服务本地 Outbox Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface OrderOutboxMapper extends BaseMapper<OrderOutboxDO> {

    /**
     * 查询到期待发布事件（已到重试时间，按 ID 升序）。
     *
     * @param page 分页参数
     * @return 待发布事件分页数据
     */
    default IPage<OrderOutboxDO> listDuePending(Page<OrderOutboxDO> page) {
        return selectPage(
                page,
                Wrappers.<OrderOutboxDO>lambdaQuery()
                        .eq(OrderOutboxDO::getPublishStatus, OutboxPublishStatusEnum.PENDING)
                        .and(
                                wrapper -> wrapper.isNull(OrderOutboxDO::getNextRetryTime)
                                                  .or()
                                                  .le(OrderOutboxDO::getNextRetryTime, LocalDateTime.now())
                        )
                        .orderByAsc(OrderOutboxDO::getId)
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
                Wrappers.<OrderOutboxDO>lambdaUpdate()
                        .set(OrderOutboxDO::getPublishStatus, OutboxPublishStatusEnum.PUBLISHED)
                        .set(OrderOutboxDO::getPublishTime, LocalDateTime.now())
                        .set(OrderOutboxDO::getLastError, null)
                        .eq(OrderOutboxDO::getId, id)
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
                Wrappers.<OrderOutboxDO>lambdaUpdate()
                        .set(OrderOutboxDO::getRetryCount, retryCount)
                        .set(OrderOutboxDO::getNextRetryTime, nextRetryTime)
                        .set(OrderOutboxDO::getLastError, lastError)
                        .set(OrderOutboxDO::getPublishStatus, status)
                        .eq(OrderOutboxDO::getId, id)
        );
    }
}
