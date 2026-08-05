package com.cloudmall.product.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.product.enums.OutboxPublishStatusEnum;

/**
 * 商品服务本地 Outbox 实体
 * <p>
 * 记录待发布商品事件（创建/更新/删除），
 * 商品变更与 Outbox 在同一本地事务提交，由定时任务发布到 RocketMQ。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_product_outbox")
public class ProductOutboxDO {

    /** Outbox 记录 ID */
    @TableId
    private Long id;

    /** 事件 ID */
    private String eventId;

    /** 聚合类型（如 PRODUCT） */
    private String aggregateType;

    /** 聚合 ID（如商品 ID） */
    private String aggregateId;

    /** 事件类型（如 ProductCreated） */
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
