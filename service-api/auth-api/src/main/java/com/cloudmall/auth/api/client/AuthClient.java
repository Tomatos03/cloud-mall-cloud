package com.cloudmall.auth.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.auth.api.request.LoginReq;
import com.cloudmall.auth.api.response.LoginResp;
import com.cloudmall.common.entity.Result;

@FeignClient(name = "auth-service", path = "/auth")
public interface AuthClient {

    @PostMapping("/login")
    Result<LoginResp> login(@RequestBody LoginReq request);
}