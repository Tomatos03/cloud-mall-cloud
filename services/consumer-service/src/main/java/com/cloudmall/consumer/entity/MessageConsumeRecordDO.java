package com.cloudmall.consumer.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.consumer.enums.ConsumeStatusEnum;

/**
 * 消息消费幂等记录实体
 * <p>
 * event_id + consumer + operation_type 唯一约束保证同一事件
 * 同一操作只消费一次，重复投递与安全重放由唯一键跳过。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_message_consume_record")
public class MessageConsumeRecordDO {

    /** 消费记录 ID */
    @TableId
    private Long id;

    /** 事件 ID */
    private String eventId;

    /** 消费者（如 consumer-service） */
    private String consumer;

    /** 消费操作类型 */
    private String operationType;

    /** 消费状态 */
    private ConsumeStatusEnum status;

    /** 重试次数 */
    private Integer retryCount;

    /** 最后错误信息 */
    private String lastError;

    /** 消费时间 */
    private LocalDateTime consumeTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
