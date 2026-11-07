package com.cloudmall.coupon.model.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.coupon.api.enums.CouponOperationActionEnum;
import com.cloudmall.coupon.api.enums.CouponOperatorEnum;
import com.cloudmall.coupon.api.enums.CouponStatusEnum;

/**
 * 模板管理流水实体
 * <p>
 * 对应 cm_coupon_operation_log 表，追加写入，记录模板创建、发布、
 * 暂停、恢复、结束、删除、库存调整和紧急撤销等管理操作。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_coupon_operation_log")
public class CouponOperationLogDO {

    /** 流水 ID */
    private Long id;

    /** 模板 ID */
    private Long couponId;

    /** 操作人 ID */
    private Long operatorId;

    /** 操作人类型 */
    private CouponOperatorEnum operatorType;

    /** 店铺 ID（平台券为 null） */
    private Long shopId;

    /** 操作前状态 */
    private CouponStatusEnum fromStatus;

    /** 操作后状态 */
    private CouponStatusEnum toStatus;

    /** 操作类型 */
    private CouponOperationActionEnum actionType;

    /** 变更摘要 */
    private String changeSummary;

    /** 原因 */
    private String reason;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
