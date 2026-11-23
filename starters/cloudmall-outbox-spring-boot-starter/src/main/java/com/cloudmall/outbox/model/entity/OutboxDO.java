package com.cloudmall.outbox.model.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.outbox.enums.OutboxStatusEnum;

/**
 * 通用本地消息 Outbox 实体。
 * <p>
 * 对应固定表 {@code cm_message_outbox}，所有服务共用，{@code app_name} 区分来源。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_message_outbox")
public class OutboxDO {

    /** 事件 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 来源服务（spring.application.name） */
    private String appName;

    /** 事件类型，建议带域前缀，如 FILE_BIND */
    private String eventType;

    /** 业务 ID */
    private String bizId;

    /** 事件载荷：字符串原样写入，对象序列化为 JSON */
    private String payload;

    /** 事件状态 */
    private OutboxStatusEnum status;

    /** 已重试次数 */
    private Integer retryCount;

    /** 下次重试时间 */
    private LocalDateTime nextRetryTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
