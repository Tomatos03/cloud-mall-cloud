package com.cloudmall.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cloudmall.auth.api.request.LoginReq;
import com.cloudmall.auth.api.request.RegisterReq;
import com.cloudmall.auth.api.response.LoginResp;
import com.cloudmall.auth.entity.AuthUserDO;
import com.cloudmall.auth.mapper.AuthUserMapper;
import com.cloudmall.auth.security.JwtTokenProvider;
import com.cloudmall.auth.service.IAuthService;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final AuthUserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResp login(LoginReq request) {
        AuthUserDO user = userMapper.selectOne(
                Wrappers.<AuthUserDO>lambdaQuery()
                        .eq(AuthUserDO::getUsername, request.getUsername())
        );
        AssertUtils.notNull(user, BizErrorCode.USER_NOT_EXISTS);
        AssertUtils.isTrue(passwordEncoder.matches(request.getPassword(), user.getPassword()), BizErrorCode.PASSWORD_ERROR);
        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), user.getUserType());

        LoginResp response = LoginResp.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .userType(user.getUserType())
                .build();
        return response;
    }

    @Override
    public Long register(RegisterReq request) {
        Long count = userMapper.selectCount(
                Wrappers.<AuthUserDO>lambdaQuery()
                        .eq(AuthUserDO::getUsername, request.getUsername())
        );
        AssertUtils.isZero(count, BizErrorCode.USER_ALREADY_EXISTS);

        AuthUserDO user = AuthUserDO.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .phone(request.getPhone())
                .email(request.getEmail())
                .userType("NORMAL")
                .status(1)
                .build();

        userMapper.insert(user);
        return user.getId();
    }
}
