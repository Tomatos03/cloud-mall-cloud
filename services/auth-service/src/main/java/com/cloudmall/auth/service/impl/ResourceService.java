package com.cloudmall.auth.service.impl;

import java.util.Collections;
import java.util.List;

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
                Wrappers.<UserRoleDO>lambdaQuery().eq(UserRoleDO::getUserId, userId)
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
        List<UserRoleDO> userRoles = userRoleMapper.selectList(
                Wrappers.<UserRoleDO>lambdaQuery().eq(UserRoleDO::getUserId, userId)
        );
        List<Long> roleIds = userRoles.stream()
                                      .map(UserRoleDO::getRoleId)
                                      .toList();

        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptyList();
        }

        List<RoleResourceDO> roleResources = roleResourceMapper.selectList(
                Wrappers.<RoleResourceDO>lambdaQuery().in(RoleResourceDO::getRoleId, roleIds)
        );
        List<Long> resourceIds = roleResources.stream()
                                              .map(RoleResourceDO::getResourceId)
                                              .toList();

        if (CollUtil.isEmpty(resourceIds)) {
            return Collections.emptyList();
        }

        List<ResourceDO> resources = resourceMapper.selectList(
                Wrappers.<ResourceDO>lambdaQuery()
                        .in(ResourceDO::getId, resourceIds)
                        .eq(ResourceDO::getEnabled, StatusEnum.ENABLED)
        );
        return resources.stream()
                        .map(ResourceDO::getCode)
                        .distinct()
                        .toList();
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

        List<ResourceDO> resources = resourceMapper.selectList(
                Wrappers.<ResourceDO>lambdaQuery()
                        .eq(type != null, ResourceDO::getType, type)
                        .eq(ResourceDO::getEnabled, StatusEnum.ENABLED)
                        .orderByAsc(ResourceDO::getSort)
        );
        return resourceConverter.toResourceRespList(resources);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createResource(ResourceReq req) {
        AssertUtils.isTrue(
                ResourceTypeEnum.isValid(req.getType()),
                BizErrorCode.INVALID_RESOURCE_TYPE
        );

        if (req.getParentId() != null) {
            AssertUtils.notNull(
                    resourceMapper.selectById(req.getParentId()),
                    BizErrorCode.DATA_NOT_FOUND
            );
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
            AssertUtils.isTrue(
                    ResourceTypeEnum.isValid(req.getType()),
                    BizErrorCode.INVALID_RESOURCE_TYPE
            );
        }
        if (req.getParentId() != null) {
            AssertUtils.notNull(
                    resourceMapper.selectById(req.getParentId()),
                    BizErrorCode.DATA_NOT_FOUND
            );
        }

        resourceConverter.updateEntity(req, existing);

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
                Wrappers.<ResourceDO>lambdaQuery().eq(ResourceDO::getParentId, resourceId)
        );
        AssertUtils.isZero(ObjectUtil.defaultIfNull(childCount, 0L), BizErrorCode.INVALID_PARAM);

        resourceMapper.deleteById(resourceId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceNodeResp> getFullResourceTree() {
        List<ResourceDO> allUiResources = resourceMapper.selectList(
                Wrappers.<ResourceDO>lambdaQuery()
                        .in(ResourceDO::getType, ResourceTypeEnum.uiTypeCodes())
                        .eq(ResourceDO::getEnabled, StatusEnum.ENABLED)
                        .orderByAsc(ResourceDO::getSort)
        );
        return buildResourceTree(allUiResources);
    }

    /**
     * 将资源列表构建为层级树
     *
     * @param resources 资源实体列表
     * @return 资源树根节点列表
     */
    static List<ResourceNodeResp> buildResourceTree(List<ResourceDO> resources) {
        if (CollUtil.isEmpty(resources)) {
            return Collections.emptyList();
        }

        List<Tree<Long>> hutoolTree = TreeUtil.build(
                resources,
                null,
                ResourceService::resourceToTree
        );
        if (CollUtil.isEmpty(hutoolTree)) {
            return Collections.emptyList();
        }
        return hutoolTree.stream()
                         .map(ResourceService::treeToResourceNode)
                         .toList();
    }

    /**
     * 将资源实体写入资源树。
     *
     * @param r 资源实体
     * @param t 资源树
     */
    private static void resourceToTree(ResourceDO r, Tree<Long> t) {
        t.setId(r.getId());
        t.setParentId(r.getParentId());
        t.setName(r.getName());
        t.setWeight(ObjectUtil.defaultIfNull(r.getSort(), 0).doubleValue());
        t.putExtra("type", r.getType());
        t.putExtra("code", r.getCode());
        t.putExtra("enabled", r.getEnabled());
    }

    /**
     * 将 Hutool Tree 节点转换为业务 ResourceNode。
     *
     * @param tree 资源树
     * @return 处理结果
     */
    static ResourceNodeResp treeToResourceNode(Tree<Long> tree) {
        ResourceResp info = ResourceResp
                .builder()
                .id(tree.getId())
                .type((String) tree.get("type"))
                .code((String) tree.get("code"))
                .name(StrUtil.toStringOrNull(tree.getName()))
                .parentId(tree.getParentId())
                .sort(((Number) ObjectUtil.defaultIfNull(tree.getWeight(), 0)).intValue())
                .enabled(BooleanUtil.isTrue((Boolean) tree.get("enabled")))
                .build();

        List<ResourceNodeResp> children = CollUtil.emptyIfNull(tree.getChildren())
                                                  .stream()
                                                  .map(ResourceService::treeToResourceNode)
                                                  .toList();

        return ResourceNodeResp.builder()
                               .resource(info)
                               .children(children)
                               .build();
    }
}
