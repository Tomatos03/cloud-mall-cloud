package com.cloudmall.user.service.impl;

import java.util.Collections;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.user.convert.ResourceConverter;
import com.cloudmall.user.entity.ResourceDO;
import com.cloudmall.user.entity.RoleResourceDO;
import com.cloudmall.user.entity.UserRoleDO;
import com.cloudmall.user.enums.ResourceTypeEnum;
import com.cloudmall.user.mapper.ResourceMapper;
import com.cloudmall.user.mapper.RoleResourceMapper;
import com.cloudmall.user.mapper.UserRoleMapper;
import com.cloudmall.user.model.req.ResourceReq;
import com.cloudmall.user.model.resp.ResourceNodeResp;
import com.cloudmall.user.model.resp.ResourceResp;
import com.cloudmall.user.service.IResourceService;

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
        List<UserRoleDO> userRoles = userRoleMapper.selectByUserId(userId);
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
        List<UserRoleDO> userRoles = userRoleMapper.selectByUserId(userId);
        List<Long> roleIds = userRoles.stream()
                                      .map(UserRoleDO::getRoleId)
                                      .toList();

        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptyList();
        }

        List<RoleResourceDO> roleResources = roleResourceMapper.selectByRoleIds(roleIds);
        List<Long> resourceIds = roleResources.stream()
                                              .map(RoleResourceDO::getResourceId)
                                              .toList();

        if (CollUtil.isEmpty(resourceIds)) {
            return Collections.emptyList();
        }

        List<ResourceDO> resources = resourceMapper.selectEnabledByIds(resourceIds);
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
        ResourceDO resourceDo = Asserts.requireNonNull(resourceMapper.selectById(resourceId));
        return resourceConverter.toResourceResp(resourceDo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceResp> listResources(String type) {
        if (type != null) {
            Asserts.isTrue(ResourceTypeEnum.isValid(type), BizError.INVALID_RESOURCE_TYPE);
        }

        List<ResourceDO> resources = resourceMapper.selectEnabledByType(type);
        return resourceConverter.toResourceRespList(resources);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createResource(ResourceReq req) {
        Asserts.isTrue(ResourceTypeEnum.isValid(req.getType()), BizError.INVALID_RESOURCE_TYPE);

        if (req.getParentId() != null) {
            Asserts.notNull(resourceMapper.selectById(req.getParentId()));
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
        ResourceDO existing = Asserts.requireNonNull(resourceMapper.selectById(id));

        if (req.getType() != null) {
            Asserts.isTrue(ResourceTypeEnum.isValid(req.getType()), BizError.INVALID_RESOURCE_TYPE);
        }
        if (req.getParentId() != null) {
            Asserts.notNull(resourceMapper.selectById(req.getParentId()));
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
        ResourceDO existing = Asserts.requireNonNull(resourceMapper.selectById(resourceId));

        Long childCount = resourceMapper.countByParentId(resourceId);
        Asserts.isZero(ObjectUtil.defaultIfNull(childCount, 0L), BizError.INVALID_PARAM);

        resourceMapper.deleteById(resourceId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceNodeResp> getFullResourceTree() {
        List<ResourceDO> allUiResources = resourceMapper.selectEnabledUiResources();
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

        List<Tree<Long>> hutoolTree = TreeUtil.build(resources, null, ResourceService::resourceToTree);
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
