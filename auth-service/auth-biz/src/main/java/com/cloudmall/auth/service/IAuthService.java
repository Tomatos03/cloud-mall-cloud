package com.cloudmall.auth.service;

import com.cloudmall.api.auth.request.LoginRequest;
import com.cloudmall.api.auth.response.LoginResponse;
import com.cloudmall.api.auth.response.UserInfoResponse;
import com.cloudmall.auth.api.request.RegisterRequest;

public interface IAuthService {
    LoginResponse login(LoginRequest request);
    Long register(RegisterRequest request);
    UserInfoResponse getUserInfo(Long userId);
}
