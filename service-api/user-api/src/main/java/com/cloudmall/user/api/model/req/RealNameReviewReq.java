package com.cloudmall.user.api.model.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内部实名认证审核请求。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealNameReviewReq {

    private Long userId;

    @NotBlank(message = "审核状态不能为空")
    private String status;

    private String rejectReason;
}
