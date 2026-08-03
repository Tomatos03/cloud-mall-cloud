package com.cloudmall.auth.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResp {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String userType;
}
