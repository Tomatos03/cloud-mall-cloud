package com.cloudmall.auth.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cloudmall.auth.api.request.LoginReq;
import com.cloudmall.auth.api.request.RegisterReq;
import com.cloudmall.auth.api.response.LoginResp;
import com.cloudmall.auth.api.response.UserInfoResp;
import com.cloudmall.auth.convert.UserConverter;
import com.cloudmall.auth.entity.UserDO;
import com.cloudmall.auth.entity.UserRoleDO;
import com.cloudmall.auth.mapper.UserMapper;
import com.cloudmall.auth.mapper.UserRoleMapper;
import com.cloudmall.auth.model.req.UserPageReq;
import com.cloudmall.auth.service.IResourceService;
import com.cloudmall.auth.service.IUserService;
import com.cloudmall.common.context.UserContext;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.jwt.service.ITokenService;

/**
 * 用户服务实现
 * <p>
 * 提供认证（登录/注册）、用户信息查询以及用户-角色关联查询。
 *
 * @author : Tomatos
 * @date : 2026/7/29
 */
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final ITokenService authTokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserConverter userConverter;
    private final IResourceService resourceService;

    /**
     * {@inheritDoc}
     */
    @Override
    public LoginResp login(LoginReq request) {
        UserDO user = userMapper.selectOne(
                Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getUsername, request.getUsername())
        );
        AssertUtils.notNull(user, BizErrorCode.USER_NOT_EXISTS);
        AssertUtils.isTrue(
                passwordEncoder.matches(request.getPassword(), user.getPassword()),
                BizErrorCode.PASSWORD_ERROR
        );

        List<String> permissionCodes = resourceService.getPermissionCodes(user.getId());

        UserContext userCtx = UserContext.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .userType(user.getTypes())
                .permissionCodes(permissionCodes)
                .build();
        String token = authTokenService.createToken(userCtx);

        LoginResp response = userConverter.toLoginResp(user);
        response.setToken(token);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long register(RegisterReq request) {
        Long count = userMapper.selectCount(
                Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getUsername, request.getUsername())
        );
        AssertUtils.isZero(count, BizErrorCode.USER_ALREADY_EXISTS);

        UserDO user = userConverter.toDO(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTypes("NORMAL");

        userMapper.insert(user);
        return user.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserInfoResp getUserInfo(Long userId) {
        UserDO user = userMapper.selectById(userId);
        AssertUtils.notNull(user, BizErrorCode.USER_NOT_EXISTS);
        return userConverter.toResp(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPage<UserInfoResp> pageUserInfos(UserPageReq req) {
        return userMapper.selectPage(req, req.buildLambdaWrapper())
                         .convert(userConverter::toResp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Long> getUserRoleIds(Long userId) {
        return userRoleMapper.selectList(
                Wrappers.<UserRoleDO>lambdaQuery()
                        .eq(UserRoleDO::getUserId, userId)
        ).stream()
         .map(UserRoleDO::getRoleId)
         .toList();
    }
}
