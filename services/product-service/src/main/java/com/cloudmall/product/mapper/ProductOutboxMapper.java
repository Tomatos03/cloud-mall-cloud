package com.cloudmall.product.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.product.entity.ProductOutboxDO;
import com.cloudmall.product.enums.OutboxPublishStatusEnum;

/**
 * 商品服务本地 Outbox Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface ProductOutboxMapper extends BaseMapper<ProductOutboxDO> {

    /**
     * 查询到期待发布事件（已到重试时间，按 ID 升序）。
     *
     * @param batchSize 批次大小
     * @return 待发布事件列表
     */
    default List<ProductOutboxDO> listDuePending(int batchSize) {
        Page<ProductOutboxDO> page = selectPage(
                new Page<>(1, batchSize),
                Wrappers.<ProductOutboxDO>lambdaQuery()
                        .eq(ProductOutboxDO::getPublishStatus, OutboxPublishStatusEnum.PENDING)
                        .and(
                                wrapper -> wrapper.isNull(ProductOutboxDO::getNextRetryTime)
                                                  .or()
                                                  .le(ProductOutboxDO::getNextRetryTime, LocalDateTime.now())
                        )
                        .orderByAsc(ProductOutboxDO::getId)
        );
        return page.getRecords();
    }

    /**
     * 标记事件已发布。
     *
     * @param id Outbox 记录 ID
     * @return 影响行数
     */
    default int markPublished(Long id) {
        return update(
                Wrappers.<ProductOutboxDO>lambdaUpdate()
                        .set(ProductOutboxDO::getPublishStatus, OutboxPublishStatusEnum.PUBLISHED)
                        .set(ProductOutboxDO::getPublishTime, LocalDateTime.now())
                        .set(ProductOutboxDO::getLastError, null)
                        .eq(ProductOutboxDO::getId, id)
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
                Wrappers.<ProductOutboxDO>lambdaUpdate()
                        .set(ProductOutboxDO::getRetryCount, retryCount)
                        .set(ProductOutboxDO::getNextRetryTime, nextRetryTime)
                        .set(ProductOutboxDO::getLastError, lastError)
                        .set(ProductOutboxDO::getPublishStatus, status)
                        .eq(ProductOutboxDO::getId, id)
        );
    }
}
