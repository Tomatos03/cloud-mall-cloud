package com.cloudmall.auth.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.auth.convert.ResourceConverter;
import com.cloudmall.auth.entity.ResourceDO;
import com.cloudmall.auth.entity.RoleResourceDO;
import com.cloudmall.auth.entity.UserRoleDO;
import com.cloudmall.auth.enums.ResourceTypeEnum;
import com.cloudmall.auth.mapper.ResourceMapper;
import com.cloudmall.auth.mapper.RoleResourceMapper;
import com.cloudmall.auth.mapper.UserRoleMapper;
import com.cloudmall.auth.model.req.ResourceReq;
import com.cloudmall.auth.model.resp.ResourceNodeResp;
import com.cloudmall.auth.model.resp.ResourceResp;
import com.cloudmall.auth.service.IResourceService;
import com.cloudmall.common.context.UserContext;
import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.mybatisplus.enums.StatusEnum;

/**
 * 资源权限统一服务实现
 * <p>
 * 提供资源（菜单/目录/按钮）的增删改查、树形结构管理，
 * 以及用户维度的角色/权限查询与判定。
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceService implements IResourceService {

    private final UserRoleMapper userRoleMapper;
    private final RoleResourceMapper roleResourceMapper;
    private final ResourceMapper resourceMapper;
    private final ResourceConverter resourceConverter;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getRoleCodes(Long userId) {
        List<UserRoleDO> userRoles = userRoleMapper.selectList(
                Wrappers.<UserRoleDO>lambdaQuery()
                        .eq(UserRoleDO::getUserId, userId)
        );
        if (CollUtil.isEmpty(userRoles)) {
            return Collections.emptyList();
        }
        return userRoles.stream()
                        .map(ur -> String.valueOf(ur.getRoleId()))
                        .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getPermissionCodes(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(
                Wrappers.<UserRoleDO>lambdaQuery()
                        .eq(UserRoleDO::getUserId, userId)
        ).stream().map(UserRoleDO::getRoleId).toList();

        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptyList();
        }

        List<Long> resourceIds = roleResourceMapper.selectList(
                Wrappers.<RoleResourceDO>lambdaQuery()
                        .in(RoleResourceDO::getRoleId, roleIds)
        ).stream().map(RoleResourceDO::getResourceId).toList();

        if (CollUtil.isEmpty(resourceIds)) {
            return Collections.emptyList();
        }

        return resourceMapper.selectList(
                Wrappers.<ResourceDO>lambdaQuery()
                        .in(ResourceDO::getId, resourceIds)
                        .eq(ResourceDO::getEnabled, StatusEnum.ENABLED)
        ).stream().map(ResourceDO::getCode).distinct().toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasRole(Long userId, String roleCode) {
        return getRoleCodes(userId).contains(roleCode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        return getPermissionCodes(userId).contains(permissionCode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceResp getResource(Long resourceId) {
        ResourceDO resourceDo = resourceMapper.selectById(resourceId);
        AssertUtils.notNull(resourceDo, BizErrorCode.DATA_NOT_FOUND);
        return resourceConverter.toResourceResp(resourceDo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceResp> listResources(String type) {
        if (type != null) {
            AssertUtils.isTrue(ResourceTypeEnum.isValid(type), BizErrorCode.INVALID_RESOURCE_TYPE);
        }

        return resourceMapper.selectList(
                Wrappers.<ResourceDO>lambdaQuery()
                        .eq(type != null, ResourceDO::getType, type)
                        .eq(ResourceDO::getEnabled, StatusEnum.ENABLED)
                        .orderByAsc(ResourceDO::getSort)
        ).stream()
         .map(resourceConverter::toResourceResp)
         .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createResource(ResourceReq req) {
        AssertUtils.isTrue(ResourceTypeEnum.isValid(req.getType()), BizErrorCode.INVALID_RESOURCE_TYPE);

        if (req.getParentId() != null) {
            AssertUtils.notNull(resourceMapper.selectById(req.getParentId()), BizErrorCode.DATA_NOT_FOUND);
        }

        ResourceDO resourceDo = ResourceDO.builder()
                .type(req.getType())
                .code(req.getCode())
                .name(req.getName())
                .parentId(req.getParentId())
                .sort(ObjectUtil.defaultIfNull(req.getSort(), 0))
                .enabled(BooleanUtil.isTrue(req.getEnabled()))
                .build();

        resourceMapper.insert(resourceDo);
        return resourceDo.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateResource(Long id, ResourceReq req) {
        ResourceDO existing = resourceMapper.selectById(id);
        AssertUtils.notNull(existing, BizErrorCode.DATA_NOT_FOUND);

        if (req.getType() != null) {
            AssertUtils.isTrue(ResourceTypeEnum.isValid(req.getType()), BizErrorCode.INVALID_RESOURCE_TYPE);
        }
        if (req.getParentId() != null) {
            AssertUtils.notNull(resourceMapper.selectById(req.getParentId()), BizErrorCode.DATA_NOT_FOUND);
        }

        BeanUtil.copyProperties(req, existing,
                CopyOptions.create()
                        .setIgnoreNullValue(true)
        );

        resourceMapper.updateById(existing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteResource(Long resourceId) {
        ResourceDO existing = resourceMapper.selectById(resourceId);
        AssertUtils.notNull(existing, BizErrorCode.DATA_NOT_FOUND);

        Long childCount = resourceMapper.selectCount(
                Wrappers.<ResourceDO>lambdaQuery()
                        .eq(ResourceDO::getParentId, resourceId)
        );
        AssertUtils.isZero(ObjectUtil.defaultIfNull(childCount, 0L), BizErrorCode.INVALID_PARAM);

        resourceMapper.deleteById(resourceId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceNodeResp> getResourceTree() {
        List<String> userPermissions = getCurrentUserPermissionCodes();
        if (CollUtil.isEmpty(userPermissions)) {
            return Collections.emptyList();
        }

        List<ResourceDO> allUiResources = resourceMapper.selectList(
                Wrappers.<ResourceDO>lambdaQuery()
                        .in(ResourceDO::getType, ResourceTypeEnum.uiTypeCodes())
                        .eq(ResourceDO::getEnabled, StatusEnum.ENABLED)
                        .orderByAsc(ResourceDO::getSort)
        );
        if (CollUtil.isEmpty(allUiResources)) {
            return Collections.emptyList();
        }

        List<ResourceDO> accessible = allUiResources
                .stream()
                .filter(r -> isAccessible(r, userPermissions))
                .toList();

        List<Tree<Long>> hutoolTree = TreeUtil.build(accessible, null, (r, t) -> {
            t.setId(r.getId());
            t.setParentId(r.getParentId());
            t.setName(r.getName());
            t.setWeight(ObjectUtil.defaultIfNull(r.getSort(), 0).doubleValue());
            t.putExtra("type", r.getType());
            t.putExtra("code", r.getCode());
            t.putExtra("enabled", r.getEnabled());
        });
        if (CollUtil.isEmpty(hutoolTree)) {
            return Collections.emptyList();
        }

        List<ResourceNodeResp> tree = hutoolTree.stream()
                                                .map(this::treeToResourceNode)
                                                .toList();

        return pruneTree(tree, new HashSet<>(userPermissions));
    }

    /**
     * 获取当前用户的权限编码列表
     */
    private List<String> getCurrentUserPermissionCodes() {
        return Optional.ofNullable(UserContextHolder.get())
                       .map(UserContext::getPermissionCodes)
                       .orElse(Collections.emptyList());
    }

    /**
     * 判断资源是否对当前用户可见
     * <p>
     * 资源无权限编码（空白）时对所有用户可见，否则需在用户权限列表中。
     *
     * @param resource  资源实体
     * @param userPerms 用户权限编码列表
     * @return true 可访问
     */
    private boolean isAccessible(ResourceDO resource, List<String> userPerms) {
        String code = resource.getCode();
        return StrUtil.isBlank(code) || userPerms.contains(code);
    }

    /**
     * 将 Hutool Tree 节点转换为业务 ResourceNode
     */
    private ResourceNodeResp treeToResourceNode(Tree<Long> tree) {
        ResourceResp info = ResourceResp.builder()
                .id(tree.getId())
                .type((String) tree.get("type"))
                .code((String) tree.get("code"))
                .name(StrUtil.toStringOrNull(tree.getName()))
                .parentId(tree.getParentId())
                .sort(tree.getWeight() != null ? ((Number) tree.getWeight()).intValue() : 0)
                .enabled(BooleanUtil.isTrue((Boolean) tree.get("enabled")))
                .build();

        List<ResourceNodeResp> children = CollUtil.emptyIfNull(tree.getChildren())
                                                  .stream()
                                                  .map(this::treeToResourceNode)
                                                  .toList();

        return ResourceNodeResp.builder()
                               .resource(info)
                               .children(children)
                               .build();
    }

    /**
     * 裁剪菜单树：移除用户无权限的节点
     * <p>
     * 如果一个节点自身不可见且所有子节点均不可见，则从树中移除。
     * 如果节点有可见子节点，即使自身不可见也保留作为容器。
     *
     * @param nodes    待裁剪的节点列表
     * @param userPerms 用户权限编码集合
     * @return 裁剪后的节点列表
     */
    private List<ResourceNodeResp> pruneTree(List<ResourceNodeResp> nodes, Set<String> userPerms) {
        if (CollUtil.isEmpty(nodes)) {
            return Collections.emptyList();
        }

        List<ResourceNodeResp> result = new ArrayList<>();
        for (ResourceNodeResp node : nodes) {
            List<ResourceNodeResp> prunedChildren = pruneTree(node.getChildren(), userPerms);
            boolean hasVisibleChildren = !prunedChildren.isEmpty();

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
}
