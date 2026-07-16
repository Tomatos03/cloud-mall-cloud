package com.cloudmall.auth.service;

import com.cloudmall.auth.api.request.LoginReq;
import com.cloudmall.auth.api.response.LoginResp;
import com.cloudmall.auth.api.request.RegisterReq;

public interface IAuthService {
    LoginResp login(LoginReq request);
    Long register(RegisterReq request);
}
