package com.cloudmall.user.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.jwt.constant.JwtClaimNames;
import com.cloudmall.jwt.service.ITokenService;
import com.cloudmall.user.api.model.req.LoginReq;
import com.cloudmall.user.api.model.resp.LoginResp;
import com.cloudmall.user.convert.UserConverter;
import com.cloudmall.user.model.entity.UserDO;
import com.cloudmall.user.service.IResourceService;
import com.cloudmall.user.service.IShopService;
import com.cloudmall.user.service.IUserService;

/**
 * 认证应用服务。
 *
 * @author : Tomatos
 * @date : 2026/7/29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthApp {

    private final IUserService userService;
    private final UserConverter userConverter;
    private final PasswordEncoder passwordEncoder;
    private final IResourceService resourceService;
    private final ITokenService authTokenService;
    private final IShopService shopService;

    /**
     * 登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    public LoginResp login(LoginReq request) {
        UserDO user = authenticate(request);
        return genLoginResp(prepareClaims(user));
    }

    /**
     * 校验登录凭证：用户存在性、密码、用户类型。 <p>
     * 用户不存在、密码错误、用户类型不匹配统一返回
     * {@link BizErrorEnum#USERNAME_OR_PASSWORD_ERROR}， 避
     * 免账号是否存在与账号类型被探测。
     *
     * @param request 登录请求
     * @return 认证通过的用户
     */
    private UserDO authenticate(LoginReq request) {
        UserDO user = Asserts.requireNonNull(
                userService.findByUsername(request.getUsername()),
                BizErrorEnum.USERNAME_OR_PASSWORD_ERROR
        );
        Asserts.isTrue(
                passwordEncoder.matches(request.getPassword(), user.getPassword()),
                BizErrorEnum.USERNAME_OR_PASSWORD_ERROR
        );
        Asserts.isEqual(user.getTypes(), request.getUserType(), BizErrorEnum.USERNAME_OR_PASSWORD_ERROR);
        return user;
    }

    @NonNull
    private LoginResp genLoginResp(Map<String, Object> claims) {
        String token = authTokenService.createToken(claims);
        return userConverter.toLoginResp(token);
    }

    private Map<String, Object> prepareClaims(UserDO user) {
        List<String> permissionCodes = resourceService.getPermissionCodes(user.getId());
        Long shopId = shopService.findShopIdByUserId(user.getId()).orElse(null);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimNames.USER_ID, user.getId());
        claims.put(JwtClaimNames.USERNAME, user.getUsername());
        claims.put(JwtClaimNames.USER_TYPE, user.getTypes());
        if (shopId != null) {
            claims.put(JwtClaimNames.SHOP_ID, shopId);
        }
        if (CollUtil.isNotEmpty(permissionCodes)) {
            claims.put(JwtClaimNames.PERMISSION_CODES, permissionCodes);
        }
        return claims;
    }
}
