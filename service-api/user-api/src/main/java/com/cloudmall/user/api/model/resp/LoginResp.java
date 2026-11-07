package com.cloudmall.user.api.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应。
 * <p>
 * 仅返回凭证 token，用户信息由前端以 token 内 userId 二次请求
 * GET /users/{id}（UserResp）获取。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResp {
    private String token;
}
