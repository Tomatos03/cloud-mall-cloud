package com.cloudmall.order.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.order.enums.OutboxPublishStatusEnum;

/**
 * 订单服务本地 Outbox 实体
 * <p>
 * 记录待发布订单事件（支付/超时/取消/全额退款），
 * 状态变更与 Outbox 在同一本地事务提交，由定时任务发布到 RocketMQ。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_order_outbox")
public class OrderOutboxDO {

    /** Outbox 记录 ID */
    @TableId
    private Long id;

    /** 事件 ID */
    private String eventId;

    /** 聚合类型（如 ORDER） */
    private String aggregateType;

    /** 聚合 ID（如父订单 ID） */
    private String aggregateId;

    /** 事件类型（如 OrderPaid） */
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
