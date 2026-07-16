package com.cloudmall.user.service.impl;

import com.cloudmall.auth.api.response.UserInfoResp;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.user.entity.auth.AuthUserDO;
import com.cloudmall.user.mapper.AuthUserMapper;
import com.cloudmall.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final AuthUserMapper authUserMapper;

    @Override
    public UserInfoResp getUserInfo(Long userId) {
        AuthUserDO user = authUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(BizErrorCode.USER_NOT_EXISTS);
        }
        UserInfoResp response = new UserInfoResp();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setUserType(user.getUserType());
        response.setStoreId(user.getStoreId());
        return response;
    }
}
