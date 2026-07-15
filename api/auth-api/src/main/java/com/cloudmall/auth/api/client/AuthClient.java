package com.cloudmall.auth.api.client;

import com.cloudmall.auth.api.request.LoginReq;
import com.cloudmall.auth.api.response.LoginResp;
import com.cloudmall.auth.api.response.UserInfoResp;
import com.cloudmall.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service", path = "/auth")
public interface AuthClient {

    @PostMapping("/login")
    Result<LoginResp> login(@RequestBody LoginReq request);

    @GetMapping("/userinfo")
    Result<UserInfoResp> getUserInfo(@RequestParam("userId") Long userId);
}
