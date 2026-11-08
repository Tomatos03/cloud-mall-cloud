package com.cloudmall.user.api.model.req;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新当前登录用户个人资料请求。
 * <p>
 * 所有字段均为可选，仅更新传入的非 null 字段。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileReq {

    @Size(max = 100, message = "昵称长度不能超过100个字符")
    private String nickname;

    @Size(max = 500, message = "头像地址长度不能超过500个字符")
    private String avatar;

    @Size(max = 30, message = "手机号长度不能超过30个字符")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Size(max = 255, message = "个性签名长度不能超过255个字符")
    private String bio;

    @Pattern(regexp = "男|女|保密", message = "性别只能是男、女或保密")
    private String gender;

    private LocalDate birthday;
}
