package com.cloudmall.user.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UpdateReq {
    private Long id;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
}