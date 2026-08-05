package com.cloudmall.auth.application;

import java.util.List;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cloudmall.auth.api.request.LoginReq;
import com.cloudmall.auth.api.response.LoginResp;
import com.cloudmall.auth.convert.UserConverter;
import com.cloudmall.auth.entity.UserDO;
import com.cloudmall.auth.service.IResourceService;
import com.cloudmall.auth.service.IUserService;
import com.cloudmall.common.context.UserContext;
import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.enums.UserTypeEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.jwt.service.ITokenService;
import com.cloudmall.user.api.client.ShopClient;
import com.cloudmall.user.api.response.ShopResp;

/**
 * 认证应用服务。
 * <p>
 * 编排涉及远程调用的登录流程：查询用户 → 校验凭证 → 从数据库加载权限码 → 查询店铺 ID（远程） → 组装上下文 → 生成 Token。
 * 本地查询与事务写库见 {@link com.cloudmall.auth.service.impl.UserService}。
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
    private final ShopClient shopClient;

    /**
     * 登录编排：查询用户 → 校验密码 → 从数据库加载权限码 → 远程查询店铺 → 组装上下文 → 生成
     * Token。
     *
     * @param request 登录请求
     * @return 登录响应（含 Token）
     */
    public LoginResp login(LoginReq request) {
        UserDO user = Asserts.requireNonNull(
                userService.findByUsername(request.getUsername()),
                BizError.USER_NOT_EXISTS
        );
        verifyPassword(user, request.getPassword());
        UserContext userCtx = prepareUserContext(user);
        return genLoginRespWithToken(userCtx, user);
    }

    @NonNull
    private LoginResp genLoginRespWithToken(UserContext userCtx, UserDO user) {
        String token = authTokenService.createToken(userCtx);
        return userConverter.toLoginResp(user, token);
    }

    private UserContext prepareUserContext(UserDO user) {
        List<String> permissionCodes = resourceService.getPermissionCodes(user.getId());
        Long shopId = resolveShopId(user);

        return UserContext.builder()
                          .userId(user.getId())
                          .username(user.getUsername())
                          .userType(ObjectUtil.defaultIfNull(user.getTypes(), UserTypeEnum.NORMAL))
                          .shopId(shopId)
                          .permissionCodes(permissionCodes)
                          .build();
    }

    /**
     * 校验用户密码。
     *
     * @param user 用户实体
     * @param rawPassword 明文密码
     */
    private void verifyPassword(UserDO user, String rawPassword) {
        Asserts.isTrue(passwordEncoder.matches(rawPassword, user.getPassword()), BizError.PASSWORD_ERROR);
    }

    /**
     * 解析用户店铺 ID：仅商家查询其名下唯一店铺，其余类型或未开店时返回 null。
     *
     * @param user 用户实体
     * @return 店铺 ID，无店铺时为 null
     */
    private Long resolveShopId(UserDO user) {
        if (user.getTypes() != UserTypeEnum.MERCHANT) {
            return null;
        }
        ShopResp shop = shopClient.getByUserId(user.getId());
        return shop != null ? shop.getId() : null;
    }
}
