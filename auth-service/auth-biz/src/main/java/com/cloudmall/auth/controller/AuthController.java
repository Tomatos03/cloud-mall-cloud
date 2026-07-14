package com.cloudmall.auth.controller;

import com.cloudmall.api.auth.request.LoginRequest;
import com.cloudmall.api.auth.response.LoginResponse;
import com.cloudmall.api.auth.response.UserInfoResponse;
import com.cloudmall.auth.api.request.RegisterRequest;
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

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/register")
    public Result<Long> register(@RequestBody @Valid RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @GetMapping("/userinfo")
    public Result<UserInfoResponse> getUserInfo(@RequestParam Long userId) {
        return Result.success(authService.getUserInfo(userId));
    }
}
