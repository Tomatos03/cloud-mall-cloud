package com.cloudmall.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.auth.api.response.UserInfoResp;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.user.entity.auth.AuthUserDO;
import com.cloudmall.user.mapper.AuthUserMapper;
import com.cloudmall.user.service.IUserService;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final AuthUserMapper authUserMapper;

    @Override
    public UserInfoResp getUserInfo(Long userId) {
        AuthUserDO user = authUserMapper.selectById(userId);
        AssertUtils.notNull(user, BizErrorCode.USER_NOT_EXISTS);
        UserInfoResp response = UserInfoResp.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .userType(user.getUserType())
                .storeId(user.getStoreId())
                .build();
        return response;
    }
}
