package com.cloudmall.auth.api.client;

import com.cloudmall.auth.api.request.LoginRequest;
import com.cloudmall.auth.api.response.LoginResponse;
import com.cloudmall.auth.api.response.UserInfoResponse;
import com.cloudmall.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service", path = "/auth")
public interface AuthClient {

    @PostMapping("/login")
    Result<LoginResponse> login(@RequestBody LoginRequest request);

    @GetMapping("/userinfo")
    Result<UserInfoResponse> getUserInfo(@RequestParam("userId") Long userId);
}
