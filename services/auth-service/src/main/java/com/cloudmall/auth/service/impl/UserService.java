package com.cloudmall.auth.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.auth.api.request.RegisterReq;
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
import com.cloudmall.auth.service.IUserService;
import com.cloudmall.common.context.UserContext;
import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.enums.UserTypeEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.mybatisplus.enums.StatusEnum;

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
    private final UserConverter userConverter;
    /**
     * {@inheritDoc}
     */
    @Override
    public UserDO findByUsername(String username) {
        return userMapper.selectOne(Wrappers.<UserDO>lambdaQuery().eq(UserDO::getUsername, username));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterReq request) {
        Asserts.isFalse(userMapper.existsByUsername(request.getUsername()), BizError.USER_ALREADY_EXISTS);
        UserDO user = addNewUser(request);
        authOutboxService.appendUserRegistered(user.getId(), user.getUsername());
        return user.getId();
    }

    @NonNull
    private UserDO addNewUser(RegisterReq request) {
        UserDO user = userConverter.toDO(request);
        user.setTypes(UserTypeEnum.NORMAL);
        userMapper.insert(user);
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserInfoResp getUserInfo(Long userId) {
        UserDO user = Asserts.requireNonNull(userMapper.selectById(userId), BizError.USER_NOT_EXISTS);
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
