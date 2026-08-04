package com.cloudmall.auth.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.auth.api.request.LoginReq;
import com.cloudmall.auth.api.response.LoginResp;

@FeignClient(name = "auth-service", contextId = "authClient", path = "/auth")
public interface AuthClient {

    /**
     * 执行用户登录认证。
     *
     * @param request 请求参数
     * @return 处理结果
     */
    @PostMapping("/login")
    LoginResp login(
            @RequestBody
            LoginReq request
    );
}
