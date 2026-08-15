package com.cloudmall.user.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResp {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String userType;
}
