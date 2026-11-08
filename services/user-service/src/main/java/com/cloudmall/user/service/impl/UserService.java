package com.cloudmall.user.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.context.UserContext;
import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.enums.UserTypeEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.rocketmq.producer.MQProducer;
import com.cloudmall.user.api.model.bo.UserRegisteredEvent;
import com.cloudmall.user.api.model.req.RegisterReq;
import com.cloudmall.user.api.model.resp.UserResp;
import com.cloudmall.user.constant.StoreConstants;
import com.cloudmall.user.convert.UserConverter;
import com.cloudmall.user.mapper.ResourceMapper;
import com.cloudmall.user.mapper.UserMapper;
import com.cloudmall.user.mapper.UserRoleMapper;
import com.cloudmall.user.model.entity.ResourceDO;
import com.cloudmall.user.model.entity.UserDO;
import com.cloudmall.user.model.entity.UserRoleDO;
import com.cloudmall.user.model.req.UserPageReq;
import com.cloudmall.user.model.resp.ResourceNodeResp;
import com.cloudmall.user.service.IUserService;

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
    private final MQProducer mqProducer;
    private final UserRoleMapper userRoleMapper;
    private final ResourceMapper resourceMapper;
    private final UserConverter userConverter;

    /** 用户注册事件 Topic */
    private static final String USER_REGISTERED_TOPIC = "USER_REGISTERED";

    private final PasswordEncoder passwordEncoder;

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDO findByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterReq request) {
        Asserts.isFalse(userMapper.existsByUsername(request.getUsername()), BizErrorEnum.USER_ALREADY_EXISTS);
        UserDO user = addNewUser(request);
        userRegisterEvent(user);
        return user.getId();
    }

    /**
     * 发送用户注册事务消息。
     *
     * @param user 注册用户
     */
    private void userRegisterEvent(UserDO user) {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                                                       .userId(user.getId())
                                                       .username(user.getUsername())
                                                       .build();
        mqProducer.sendInTransaction(
                USER_REGISTERED_TOPIC,
                "UserRegistered",
                event,
                String.valueOf(user.getId())
        );
    }

    /**
     * 构建并落库新用户。
     *
     * @param request 注册请求
     * @return 新用户
     */
    @NonNull
    private UserDO addNewUser(RegisterReq request) {
        UserDO user = userConverter.toDO(request);
        user.setTypes(UserTypeEnum.NORMAL);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userMapper.insert(user);
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserResp getUser(Long userId) {
        UserDO user = Asserts.requireNonNull(userMapper.selectById(userId), BizErrorEnum.USER_NOT_EXISTS);
        return userConverter.toResp(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserResp> listByIds(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        List<UserDO> users = userMapper.selectByIds(userIds);
        return userConverter.toRespList(users);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPage<UserResp> pageUser(UserPageReq req) {
        IPage<UserDO> page = userMapper.selectUserPage(req);
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
        List<ResourceDO> allUiResources = resourceMapper.selectEnabledUiResources();
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
        List<UserRoleDO> userRoles = userRoleMapper.selectByUserId(userId);
        return userRoles.stream()
                        .map(UserRoleDO::getRoleId)
                        .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantMerchant(Long userId) {
        UserDO user = Asserts.requireNonNull(userMapper.selectById(userId), BizErrorEnum.USER_NOT_EXISTS);
        if (user.getTypes() != UserTypeEnum.MERCHANT) {
            user.setTypes(UserTypeEnum.MERCHANT);
            userMapper.updateById(user);
        }
        List<UserRoleDO> roles = userRoleMapper.selectByUserId(userId);
        boolean hasMerchantRole = roles.stream()
                                       .anyMatch(role -> role.getRoleId() == StoreConstants.MERCHANT_ROLE_ID);
        if (!hasMerchantRole) {
            userRoleMapper.insert(
                    UserRoleDO.builder()
                              .userId(userId)
                              .roleId(StoreConstants.MERCHANT_ROLE_ID)
                              .build()
            );
        }
    }
}
