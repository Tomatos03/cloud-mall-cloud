package com.cloudmall.seckill.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀商品审核请求
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityProductAuditReq {

    /** 是否审核通过 */
    private boolean approved;

    /** 审核意见/驳回原因 */
    private String auditRemark;
}
