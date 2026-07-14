package com.cloudmall.api.auth.response;

import lombok.Data;

@Data
public class UserInfoResponse {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Long storeId;
    private String userType;
}
