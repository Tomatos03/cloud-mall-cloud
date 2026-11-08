package com.cloudmall.user.api.model.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提交实名认证请求。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitRealNameReq {

    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 50, message = "真实姓名长度为2-50个字符")
    private String realName;

    @NotBlank(message = "身份证号不能为空")
    @Size(min = 18, max = 18, message = "身份证号必须为18位")
    private String idCardNo;
}
