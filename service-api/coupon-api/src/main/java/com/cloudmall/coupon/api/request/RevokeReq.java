package com.cloudmall.coupon.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户券撤销请求
 * <p>
 * 运营端紧急撤销指定模板下可撤销的用户券，原因必填并记录流水。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokeReq {

    /** 撤销原因 */
    @NotBlank
    private String reason;
}
