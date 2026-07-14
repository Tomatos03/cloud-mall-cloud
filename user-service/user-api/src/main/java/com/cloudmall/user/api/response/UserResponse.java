package com.cloudmall.user.api.response;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private String userType;
    private Integer status;
}
