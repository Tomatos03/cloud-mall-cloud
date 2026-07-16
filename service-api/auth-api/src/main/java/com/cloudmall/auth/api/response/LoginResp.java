package com.cloudmall.auth.api.response;

import lombok.Data;

@Data
public class LoginResp {
    private String token;
    private Long userId;
    private String username;
    private String userType;
}
