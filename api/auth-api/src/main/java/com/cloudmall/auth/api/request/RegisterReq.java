package com.cloudmall.auth.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterReq {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String nickname;
    private String phone;
    private String email;
}
