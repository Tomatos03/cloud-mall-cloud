package com.cloudmall.user.api.model.req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商家入驻审核请求。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreReviewReq {

    /** 入驻申请 ID */
    @NotNull
    private Long applicationId;

    /** 是否通过 */
    @NotNull
    private Boolean approved;

    /** 审核意见/驳回原因 */
    @Size(max = 500)
    private String remark;
}
