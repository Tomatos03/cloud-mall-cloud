package com.cloudmall.auth.service.impl;

import java.util.List;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;

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
        RoleDO existing = roleMapper.selectById(roleId);
        AssertUtils.notNull(existing, BizErrorCode.DATA_NOT_FOUND);

        BeanUtil.copyProperties(req, existing, CopyOptions.create().setIgnoreNullValue(true));
        roleMapper.updateById(existing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        RoleDO existing = roleMapper.selectById(roleId);
        AssertUtils.notNull(existing, BizErrorCode.DATA_NOT_FOUND);
        AssertUtils.isTrue(!BooleanUtil.isTrue(existing.getBuiltIn()), BizErrorCode.INVALID_PARAM);

        userRoleMapper.delete(
                Wrappers.<UserRoleDO>lambdaQuery()
                        .eq(UserRoleDO::getRoleId, roleId)
        );
        roleResourceMapper.delete(
                Wrappers.<RoleResourceDO>lambdaQuery()
                        .eq(RoleResourceDO::getRoleId, roleId)
        );
        roleMapper.deleteById(roleId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoleResp getRole(Long roleId) {
        RoleDO roleDo = roleMapper.selectById(roleId);
        AssertUtils.notNull(roleDo, BizErrorCode.DATA_NOT_FOUND);
        return roleConverter.toRoleResp(roleDo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPage<RoleResp> pageRoles(RolePageReq req) {
        return roleMapper.selectPage(req, req.buildLambdaWrapper())
                         .convert(roleConverter::toRoleResp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignResources(Long roleId, List<Long> resourceIds) {
        AssertUtils.notNull(roleMapper.selectById(roleId), BizErrorCode.DATA_NOT_FOUND);

        roleResourceMapper.delete(
                Wrappers.<RoleResourceDO>lambdaQuery()
                        .eq(RoleResourceDO::getRoleId, roleId)
        );
        if (CollUtil.isNotEmpty(resourceIds)) {
            List<RoleResourceDO> list = resourceIds.stream()
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
        return roleResourceMapper.selectList(
                Wrappers.<RoleResourceDO>lambdaQuery()
                        .eq(RoleResourceDO::getRoleId, roleId)
        ).stream()
         .map(RoleResourceDO::getResourceId)
         .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(
                Wrappers.<UserRoleDO>lambdaQuery()
                        .eq(UserRoleDO::getUserId, userId)
        );
        if (CollUtil.isNotEmpty(roleIds)) {
            List<UserRoleDO> list = roleIds.stream()
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
        userRoleMapper.delete(
                Wrappers.<UserRoleDO>lambdaQuery()
                        .eq(UserRoleDO::getRoleId, roleId)
        );
        if (CollUtil.isNotEmpty(userIds)) {
            List<UserRoleDO> list = userIds.stream()
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
        return userRoleMapper.selectList(
                                     Wrappers.<UserRoleDO>lambdaQuery()
                                             .eq(UserRoleDO::getRoleId, roleId)
                             ).stream()
                             .map(UserRoleDO::getUserId)
                             .toList();
    }
}
