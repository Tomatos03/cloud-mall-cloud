package com.cloudmall.auth.service.impl;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.auth.convert.RoleConverter;
import com.cloudmall.auth.entity.RoleDO;
import com.cloudmall.auth.entity.RoleResourceDO;
import com.cloudmall.auth.entity.UserRoleDO;
import com.cloudmall.auth.mapper.RoleMapper;
import com.cloudmall.auth.mapper.RoleResourceMapper;
import com.cloudmall.auth.mapper.UserRoleMapper;
import com.cloudmall.auth.model.req.RolePageReq;
import com.cloudmall.auth.model.req.RoleReq;
import com.cloudmall.auth.model.resp.RoleResp;
import com.cloudmall.auth.service.IRoleService;
import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;

/**
 * 角色管理服务实现
 * <p>
 * 提供角色的增删改查以及角色-用户、角色-资源的关联管理。
 *
 * @author : Tomatos
 * @date : 2026/7/29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {

    private final RoleMapper roleMapper;
    private final RoleResourceMapper roleResourceMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleConverter roleConverter;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRole(String name, String description, boolean enabled) {
        RoleDO roleDo = RoleDO.builder()
                              .name(name)
                              .description(description)
                              .enabled(enabled)
                              .build();
        roleMapper.insert(roleDo);
        return roleDo.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long roleId, RoleReq req) {
        RoleDO existing = Asserts.requireNonNull(roleMapper.selectById(roleId));

        roleConverter.updateEntity(req, existing);
        roleMapper.updateById(existing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        RoleDO existing = Asserts.requireNonNull(roleMapper.selectById(roleId));
        Asserts.isTrue(!BooleanUtil.isTrue(existing.getBuiltIn()), BizError.INVALID_PARAM);

        userRoleMapper.deleteByRoleId(roleId);
        roleResourceMapper.deleteByRoleId(roleId);
        roleMapper.deleteById(roleId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoleResp getRole(Long roleId) {
        RoleDO roleDo = Asserts.requireNonNull(roleMapper.selectById(roleId));
        return roleConverter.toRoleResp(roleDo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPage<RoleResp> pageRoles(RolePageReq req) {
        IPage<RoleDO> page = roleMapper.selectPage(req, req.buildLambdaWrapper());
        return page.convert(roleConverter::toRoleResp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignResources(Long roleId, List<Long> resourceIds) {
        Asserts.notNull(roleMapper.selectById(roleId));

        roleResourceMapper.deleteByRoleId(roleId);
        if (CollUtil.isNotEmpty(resourceIds)) {
            List<RoleResourceDO> list = resourceIds
                    .stream()
                    .map(resourceId -> RoleResourceDO.builder()
                                                     .roleId(roleId)
                                                     .resourceId(resourceId)
                                                     .build())
                    .toList();
            list.forEach(roleResourceMapper::insert);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Long> getRoleResourceIds(Long roleId) {
        List<RoleResourceDO> roleResources = roleResourceMapper.selectByRoleId(roleId);
        return roleResources.stream()
                            .map(RoleResourceDO::getResourceId)
                            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteByUserId(userId);
        if (CollUtil.isNotEmpty(roleIds)) {
            List<UserRoleDO> list = roleIds
                    .stream()
                    .map(roleId -> UserRoleDO.builder()
                                             .userId(userId)
                                             .roleId(roleId)
                                             .build())
                    .toList();
            list.forEach(userRoleMapper::insert);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleUsers(Long roleId, List<Long> userIds) {
        userRoleMapper.deleteByRoleId(roleId);
        if (CollUtil.isNotEmpty(userIds)) {
            List<UserRoleDO> list = userIds
                    .stream()
                    .map(userId -> UserRoleDO.builder()
                                             .roleId(roleId)
                                             .userId(userId)
                                             .build())
                    .toList();
            list.forEach(userRoleMapper::insert);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Long> getRoleUserIds(Long roleId) {
        List<UserRoleDO> userRoles = userRoleMapper.selectByRoleId(roleId);
        return userRoles.stream()
                        .map(UserRoleDO::getUserId)
                        .toList();
    }
}
