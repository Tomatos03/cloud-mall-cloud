package com.cloudmall.coupon.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.coupon.enums.UserCouponLogSourceEnum;
import com.cloudmall.coupon.enums.UserCouponOperationTypeEnum;
import com.cloudmall.coupon.enums.UserCouponStatusEnum;

/**
 * 用户券状态流水实体
 * <p>
 * 对应 cm_user_coupon_log 表，追加写入，不允许修改和删除。
 * 唯一约束 (event_id, operation_type) 构成操作幂等依据，
 * 同时支撑状态审计和基础统计。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_user_coupon_log")
public class UserCouponLogDO {

    /** 流水 ID */
    private Long id;

    /** 用户券 ID */
    private Long userCouponId;

    /** 模板 ID */
    private Long couponId;

    /** 用户 ID */
    private Long userId;

    /** 相关订单号 */
    private String orderNo;

    /** 事件/操作幂等 ID */
    private String eventId;

    /** 操作类型 */
    private UserCouponOperationTypeEnum operationType;

    /** 原状态 */
    private UserCouponStatusEnum fromStatus;

    /** 新状态 */
    private UserCouponStatusEnum toStatus;

    /** 来源 */
    private UserCouponLogSourceEnum source;

    /** 操作人 ID（系统事件为空） */
    private Long operatorId;

    /** 原因 */
    private String reason;

    /** 发生时间 */
    private LocalDateTime createTime;
}
