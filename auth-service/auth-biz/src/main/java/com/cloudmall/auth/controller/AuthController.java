package com.cloudmall.auth.controller;

import com.cloudmall.auth.api.request.LoginReq;
import com.cloudmall.auth.api.response.LoginResp;
import com.cloudmall.auth.api.response.UserInfoResp;
import com.cloudmall.auth.api.request.RegisterReq;
import com.cloudmall.auth.service.IAuthService;
import com.cloudmall.common.entity.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/sessions")
    public Result<LoginResp> login(@RequestBody @Valid LoginReq request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/users")
    public Result<Long> register(@RequestBody @Valid RegisterReq request) {
        return Result.success(authService.register(request));
    }

    @GetMapping("/users/{userId}")
    public Result<UserInfoResp> getUserInfo(@PathVariable Long userId) {
        return Result.success(authService.getUserInfo(userId));
    }
}
