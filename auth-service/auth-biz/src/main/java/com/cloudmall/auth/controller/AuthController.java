package com.cloudmall.auth.controller;

import com.cloudmall.auth.api.request.LoginReq;
import com.cloudmall.auth.api.request.RegisterReq;
import com.cloudmall.auth.api.response.LoginResp;
import com.cloudmall.auth.service.IAuthService;
import com.cloudmall.common.entity.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/login")
    public Result<LoginResp> login(@RequestBody @Valid LoginReq request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/register")
    public Result<Long> register(@RequestBody @Valid RegisterReq request) {
        return Result.success(authService.register(request));
    }
}
