package com.cloudmall.outbox.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.outbox.enums.OutboxPublishStatusEnum;

/**
 * 通用本地消息 Outbox 实体
 * <p>
 * 所有服务共用的本地消息表（app_name 区分来源），
 * 业务变更与 Outbox 在同一本地事务提交，由定时任务发布到 RocketMQ。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_message_outbox")
public class OutboxDO {

    /** Outbox 记录 ID */
    @TableId
    private Long id;

    /** 来源服务（spring.application.name） */
    private String appName;

    /** 事件 ID（全局 UUID，消费端幂等键） */
    private String eventId;

    /** 目标 RocketMQ Topic */
    private String topic;

    /** 业务类型（如 ORDER） */
    private String bizType;

    /** 业务 ID（如父订单号） */
    private String bizId;

    /** 事件类型（同时作为 RocketMQ tag） */
    private String eventType;

    /** 事件负载（JSON） */
    private String payload;

    /** 发布状态 */
    private OutboxPublishStatusEnum publishStatus;

    /** 重试次数 */
    private Integer retryCount;

    /** 下次重试时间 */
    private LocalDateTime nextRetryTime;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 最后错误信息 */
    private String lastError;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
