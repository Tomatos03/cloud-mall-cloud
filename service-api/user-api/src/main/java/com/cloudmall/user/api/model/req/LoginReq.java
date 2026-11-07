package com.cloudmall.user.api.model.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.common.enums.UserTypeEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginReq {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 用户类型（NORMAL / ADMIN / MERCHANT）。
     * <p>
     * 反序列化由 {@link UserTypeEnum#fromCode(String)}（@JsonCreator）按编码解析，
     * 序列化由 {@link UserTypeEnum#getCode()}（@JsonValue）输出编码，
     * JSON 侧以编码字符串传输，如 "NORMAL"。
     */
    @NotNull(message = "用户类型不能为空")
    private UserTypeEnum userType;
}
