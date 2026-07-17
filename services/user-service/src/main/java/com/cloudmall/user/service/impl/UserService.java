package com.cloudmall.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.auth.api.response.UserInfoResp;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.user.convert.UserConverter;
import com.cloudmall.user.entity.auth.AuthUserDO;
import com.cloudmall.user.mapper.AuthUserMapper;
import com.cloudmall.user.service.IUserService;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final AuthUserMapper authUserMapper;
    private final UserConverter userConverter;

    @Override
    public UserInfoResp getUserInfo(Long userId) {
        AuthUserDO user = authUserMapper.selectById(userId);
        AssertUtils.notNull(user, BizErrorCode.USER_NOT_EXISTS);
        return userConverter.toResp(user);
    }
}
