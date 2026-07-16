package com.cloudmall.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.auth.api.request.LoginReq;
import com.cloudmall.auth.api.response.LoginResp;
import com.cloudmall.auth.api.request.RegisterReq;
import com.cloudmall.auth.entity.AuthUserDO;
import com.cloudmall.auth.mapper.AuthUserMapper;
import com.cloudmall.auth.security.JwtTokenProvider;
import com.cloudmall.auth.service.IAuthService;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final AuthUserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResp login(LoginReq request) {
        AuthUserDO user = userMapper.selectOne(
                new LambdaQueryWrapper<AuthUserDO>()
                        .eq(AuthUserDO::getUsername, request.getUsername())
        );
        if (user == null) {
            throw new BizException(BizErrorCode.USER_NOT_EXISTS);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(BizErrorCode.PASSWORD_ERROR);
        }
        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), user.getUserType());

        LoginResp response = new LoginResp();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setUserType(user.getUserType());
        return response;
    }

    @Override
    public Long register(RegisterReq request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<AuthUserDO>()
                        .eq(AuthUserDO::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new BizException(BizErrorCode.USER_ALREADY_EXISTS);
        }

        AuthUserDO user = new AuthUserDO();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setUserType("NORMAL");
        user.setStatus(1);

        userMapper.insert(user);
        return user.getId();
    }
}
