package com.cloudmall.auth.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.auth.api.request.LoginReq;
import com.cloudmall.auth.api.request.RegisterReq;
import com.cloudmall.auth.api.response.LoginResp;
import com.cloudmall.auth.api.response.UserInfoResp;
import com.cloudmall.auth.convert.UserConverter;
import com.cloudmall.auth.entity.ResourceDO;
import com.cloudmall.auth.entity.UserDO;
import com.cloudmall.auth.entity.UserRoleDO;
import com.cloudmall.auth.enums.ResourceTypeEnum;
import com.cloudmall.auth.mapper.ResourceMapper;
import com.cloudmall.auth.mapper.UserMapper;
import com.cloudmall.auth.mapper.UserRoleMapper;
import com.cloudmall.auth.model.req.UserPageReq;
import com.cloudmall.auth.model.resp.ResourceNodeResp;
import com.cloudmall.auth.service.IAuthOutboxService;
import com.cloudmall.auth.service.IResourceService;
import com.cloudmall.auth.service.IUserService;
import com.cloudmall.common.context.UserContext;
import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.enums.UserTypeEnum;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.jwt.service.ITokenService;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.user.api.client.ShopClient;
import com.cloudmall.user.api.response.ShopResp;

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
    private final IAuthOutboxService authOutboxService;
    private final UserRoleMapper userRoleMapper;
    private final ResourceMapper resourceMapper;
    private final ITokenService authTokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserConverter userConverter;
    private final IResourceService resourceService;
    private final ShopClient shopClient;
    /**
     * {@inheritDoc}
     */
    @Override
    public LoginResp login(LoginReq request) {
        UserDO user = AssertUtils.requireNonNull(
                userMapper.selectOne(
                        Wrappers.<UserDO>lambdaQuery()
                                .eq(UserDO::getUsername, request.getUsername())
                ),
                BizErrorCode.USER_NOT_EXISTS
        );
        AssertUtils.isTrue(
                passwordEncoder.matches(request.getPassword(), user.getPassword()),
                BizErrorCode.PASSWORD_ERROR
        );

        List<String> permissionCodes = resolvePermissionCodes(
                user,
                resourceService.getPermissionCodes(user.getId())
        );

        UserContext userCtx = UserContext
                .builder()
                .userId(user.getId())
                .username(user.getUsername())
                .userType(ObjectUtil.defaultIfNull(user.getTypes(), UserTypeEnum.NORMAL))
                .shopId(resolveShopId(user))
                .permissionCodes(permissionCodes)
                .build();
        String token = authTokenService.createToken(userCtx);

        LoginResp response = userConverter.toLoginResp(user);
        response.setToken(token);
        return response;
    }

    /** 商家默认权限码 */
    private static final List<String> MERCHANT_PERMISSIONS = List.of(
            "coupon:template:create",
            "coupon:template:view",
            "coupon:template:publish",
            "coupon:template:pause",
            "coupon:template:end",
            "coupon:template:delete",
            "coupon:stats:view"
    );

    /**
     * 解析用户权限码：角色权限 + 按用户类型注入的默认权限。 商家注入店铺券管理权限，管理员注入超级权限
     * 。
     *
     * @param user 用户实体
     * @param roleCodes 角色关联权限码
     * @return 权限码列表
     */
    private List<String> resolvePermissionCodes(UserDO user, List<String> roleCodes) {
        Set<String> codes = new HashSet<>();
        if (CollUtil.isNotEmpty(roleCodes)) {
            codes.addAll(roleCodes);
        }
        if (user.getTypes() == UserTypeEnum.MERCHANT) {
            codes.addAll(MERCHANT_PERMISSIONS);
        } else if (user.getTypes() == UserTypeEnum.ADMIN) {
            codes.add("*");
        }
        return new ArrayList<>(codes);
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

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterReq request) {
        Long count = userMapper.selectCount(
                Wrappers.<UserDO>lambdaQuery().eq(UserDO::getUsername, request.getUsername())
        );
        AssertUtils.isZero(count, BizErrorCode.USER_ALREADY_EXISTS);

        UserDO user = userConverter.toDO(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTypes(UserTypeEnum.NORMAL);

        userMapper.insert(user);
        authOutboxService.appendUserRegistered(user.getId(), user.getUsername());
        return user.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserInfoResp getUserInfo(Long userId) {
        UserDO user = AssertUtils.requireNonNull(
                userMapper.selectById(userId),
                BizErrorCode.USER_NOT_EXISTS
        );
        return userConverter.toResp(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPage<UserInfoResp> pageUserInfos(UserPageReq req) {
        IPage<UserDO> page = userMapper.selectPage(req, req.buildLambdaWrapper());
        return page.convert(userConverter::toResp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceNodeResp> getMenuResourceTree() {
        List<String> userPermissions = getCurrentUserPermissionCodes();
        if (CollUtil.isEmpty(userPermissions)) {
            return Collections.emptyList();
        }

        List<ResourceDO> accessible = loadAccessibleUiResources(userPermissions);
        List<ResourceNodeResp> tree = ResourceService.buildResourceTree(accessible);
        return pruneTree(tree, new HashSet<>(userPermissions));
    }

    /**
     * 获取当前用户的权限编码列表。
     *
     * @return 数据列表
     */
    private List<String> getCurrentUserPermissionCodes() {
        return Optional.ofNullable(UserContextHolder.get())
                       .map(UserContext::getPermissionCodes)
                       .orElse(Collections.emptyList());
    }

    /**
     * 加载当前用户有权限访问的 UI 资源列表。
     *
     * @param userPermissions 用户权限编码列表
     * @return 数据列表
     */
    private List<ResourceDO> loadAccessibleUiResources(List<String> userPermissions) {
        List<ResourceDO> allUiResources = resourceMapper.selectList(
                Wrappers.<ResourceDO>lambdaQuery()
                        .in(ResourceDO::getType, ResourceTypeEnum.uiTypeCodes())
                        .eq(ResourceDO::getEnabled, StatusEnum.ENABLED)
                        .orderByAsc(ResourceDO::getSort)
        );
        if (CollUtil.isEmpty(allUiResources)) {
            return Collections.emptyList();
        }
        return allUiResources.stream()
                             .filter(r -> isAccessible(r, userPermissions))
                             .toList();
    }

    /**
     * 判断资源是否对当前用户可见。
     *
     * @param resource 资源实体
     * @param userPerms 用户权限集合
     * @return 是否满足条件
     */
    private boolean isAccessible(ResourceDO resource, List<String> userPerms) {
        String code = resource.getCode();
        return StrUtil.isBlank(code) || userPerms.contains(code);
    }

    /**
     * 裁剪菜单树，移除用户无权限的节点。
     *
     * @param nodes 资源节点列表
     * @param userPerms 用户权限集合
     * @return 数据列表
     */
    private List<ResourceNodeResp> pruneTree(List<ResourceNodeResp> nodes, Set<String> userPerms) {
        if (CollUtil.isEmpty(nodes)) {
            return Collections.emptyList();
        }

        List<ResourceNodeResp> result = new ArrayList<>();
        for (ResourceNodeResp node : nodes) {
            List<ResourceNodeResp> prunedChildren = pruneTree(node.getChildren(), userPerms);
            boolean hasVisibleChildren = CollUtil.isNotEmpty(prunedChildren);

            String code = node.getResource().getCode();
            boolean selfAccessible = StrUtil.isBlank(code) || userPerms.contains(code);

            if (hasVisibleChildren || selfAccessible) {
                ResourceNodeResp prunedNode = ResourceNodeResp.builder()
                                                              .resource(node.getResource())
                                                              .children(prunedChildren)
                                                              .build();
                result.add(prunedNode);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Long> getUserRoleIds(Long userId) {
        List<UserRoleDO> userRoles = userRoleMapper.selectList(
                Wrappers.<UserRoleDO>lambdaQuery().eq(UserRoleDO::getUserId, userId)
        );
        return userRoles.stream()
                        .map(UserRoleDO::getRoleId)
                        .toList();
    }
}
