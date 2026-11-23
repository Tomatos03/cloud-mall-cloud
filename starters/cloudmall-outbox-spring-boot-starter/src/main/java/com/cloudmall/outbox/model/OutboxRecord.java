package com.cloudmall.outbox.model;

import cn.hutool.core.util.ObjectUtil;

import com.cloudmall.outbox.model.entity.OutboxDO;

/**
 * Outbox 事件读取视图。
 *
 * @param id 事件 ID
 * @param appName 来源服务
 * @param eventType 事件类型
 * @param bizId 业务 ID
 * @param payload 事件载荷
 * @param retryCount 已重试次数
 * @author : Tomatos
 */
public record OutboxRecord(
    Long id,
    String appName,
    String eventType,
    String bizId,
    String payload,
    int retryCount
) {

    /**
     * 由实体转换为读取视图。
     *
     * @param source 实体
     * @return 读取视图
     */
    public static OutboxRecord from(OutboxDO source) {
        return new OutboxRecord(
                source.getId(),
                source.getAppName(),
                source.getEventType(),
                source.getBizId(),
                source.getPayload(),
                ObjectUtil.defaultIfNull(source.getRetryCount(), 0)
        );
    }
}
