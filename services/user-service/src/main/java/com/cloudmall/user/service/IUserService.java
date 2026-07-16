package com.cloudmall.user.service;

import com.cloudmall.auth.api.response.UserInfoResp;

public interface IUserService {
    UserInfoResp getUserInfo(Long userId);
}
