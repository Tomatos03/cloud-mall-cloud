package com.cloudmall.user.api.model.resp;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户的完整个人资料。
 * <p>
 * 昵称/头像/手机号/邮箱/签名来自 cm_user，性别/生日来自 cm_user_profile。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResp {

    private Long userId;

    private String username;

    private String nickname;

    private String avatar;

    private String phone;

    private String email;

    private String bio;

    private String gender;

    private LocalDate birthday;

    private String userType;
}
