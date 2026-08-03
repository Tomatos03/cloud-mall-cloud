package com.cloudmall.coupon.api.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板操作流水响应
 * <p>
 * 记录模板创建、编辑、发布、暂停、恢复、结束、删除、撤销等操作审计。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponOperationLogResp {

    /** 流水 ID */
    private Long id;

    /** 模板 ID */
    private Long couponId;

    /** 操作人 ID */
    private Long operatorId;

    /** 操作人类型 code */
    private String operatorType;

    /** 店铺 ID（平台券为 null） */
    private Long shopId;

    /** 操作前状态 code */
    private String fromStatus;

    /** 操作后状态 code */
    private String toStatus;

    /** 操作类型 code */
    private String actionType;

    /** 变更摘要 */
    private String changeSummary;

    /** 原因 */
    private String reason;

    /** 创建时间 */
    private LocalDateTime createTime;
}
