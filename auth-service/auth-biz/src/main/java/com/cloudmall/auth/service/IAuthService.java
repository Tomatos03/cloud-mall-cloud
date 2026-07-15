package com.cloudmall.auth.service;

import com.cloudmall.auth.api.request.LoginRequest;
import com.cloudmall.auth.api.response.LoginResponse;
import com.cloudmall.auth.api.response.UserInfoResponse;
import com.cloudmall.auth.api.request.RegisterRequest;

public interface IAuthService {
    LoginResponse login(LoginRequest request);
    Long register(RegisterRequest request);
    UserInfoResponse getUserInfo(Long userId);
}
