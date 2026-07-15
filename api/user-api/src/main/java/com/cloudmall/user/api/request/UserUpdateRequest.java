package com.cloudmall.user.api.request;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private Long id;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
}
