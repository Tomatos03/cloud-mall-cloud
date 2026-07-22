package com.cloudmall.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cloudmall.auth.api.request.LoginReq;
import com.cloudmall.auth.api.request.RegisterReq;
import com.cloudmall.auth.api.response.LoginResp;
import com.cloudmall.auth.entity.AuthUserDO;
import com.cloudmall.auth.convert.AuthConverter;
import com.cloudmall.auth.mapper.AuthUserMapper;
import com.cloudmall.jwt.token.JwtTokenTemplate;
import com.cloudmall.auth.service.IAuthService;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.common.utils.AssertUtils;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final AuthUserMapper userMapper;
    private final JwtTokenTemplate jwtTokenTemplate;
    private final PasswordEncoder passwordEncoder;
    private final AuthConverter authConverter;

    @Override
    public LoginResp login(LoginReq request) {
        AuthUserDO user = userMapper.selectOne(
                Wrappers.<AuthUserDO>lambdaQuery()
                        .eq(AuthUserDO::getUsername, request.getUsername())
        );
        AssertUtils.notNull(user, BizErrorCode.USER_NOT_EXISTS);
        AssertUtils.isTrue(passwordEncoder.matches(request.getPassword(), user.getPassword()), BizErrorCode.PASSWORD_ERROR);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("userType", user.getUserType());
        String token = jwtTokenTemplate.createToken(claims);

        LoginResp response = authConverter.toLoginResp(user);
        response.setToken(token);
        return response;
    }

    @Override
    public Long register(RegisterReq request) {
        Long count = userMapper.selectCount(
                Wrappers.<AuthUserDO>lambdaQuery()
                        .eq(AuthUserDO::getUsername, request.getUsername())
        );
        AssertUtils.isZero(count, BizErrorCode.USER_ALREADY_EXISTS);

        AuthUserDO user = authConverter.toDO(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserType("NORMAL");
        user.setStatus(StatusEnum.ENABLED);

        userMapper.insert(user);
        return user.getId();
    }
}
