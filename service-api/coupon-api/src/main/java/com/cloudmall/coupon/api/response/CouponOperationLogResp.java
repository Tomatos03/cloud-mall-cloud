package com.cloudmall.coupon.api.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.coupon.enums.CouponOperationActionEnum;
import com.cloudmall.coupon.enums.CouponOperatorEnum;
import com.cloudmall.coupon.enums.CouponStatusEnum;

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

    /** 操作人类型 */
    private CouponOperatorEnum operator;

    /** 店铺 ID（平台券为 null） */
    private Long shopId;

    /** 操作前状态 */
    private CouponStatusEnum fromStatus;

    /** 操作后状态 */
    private CouponStatusEnum toStatus;

    /** 操作类型 */
    private CouponOperationActionEnum action;
    /** 变更摘要 */
    private String changeSummary;

    /** 原因 */
    private String reason;

    /** 创建时间 */
    private LocalDateTime createTime;
}
