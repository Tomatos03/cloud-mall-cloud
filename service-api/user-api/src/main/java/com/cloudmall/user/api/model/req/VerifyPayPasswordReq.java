package com.cloudmall.user.api.model.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 校验支付密码请求。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPayPasswordReq {

    @NotBlank(message = "支付密码不能为空")
    @Pattern(regexp = "\\d{6}", message = "支付密码必须为6位数字")
    private String password;
}
