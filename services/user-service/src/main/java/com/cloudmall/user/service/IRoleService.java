package com.cloudmall.user.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.user.model.req.RolePageReq;
import com.cloudmall.user.model.req.RoleReq;
import com.cloudmall.user.model.resp.RoleResp;

/**
 * 角色管理接口
 * <p>
 * 定义角色的增删改查以及角色-权限分配。
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
public interface IRoleService {

    /**
     * 创建角色
     *
     * @param name 角色名称
     * @param description 角色描述
     * @param enabled 是否启用
     * @return 新角色 ID
     */
    Long createRole(String name, String description, boolean enabled);

    /**
     * 更新角色信息 <p>
     * 仅传入需要更新的字段即可，未传的字段保持不变。
     *
     * @param roleId 角色 ID
     * @param req 更新请求（为 null 的字段不更新）
     */
    void updateRole(Long roleId, RoleReq req);

    /**
     * 删除角色 <p>
     * 删除角色后自动清理关联的 user-role 和 role-resource 关系。
     *
     * @param roleId 角色 ID
     */
    void deleteRole(Long roleId);

    /**
     * 根据 ID 查询角色
     *
     * @param roleId 角色 ID
     * @return 角色信息
     */
    RoleResp getRole(Long roleId);

    /**
     * 分页查询角色
     *
     * @param req 分页及筛选条件
     * @return 分页角色信息
     */
    IPage<RoleResp> pageRoles(RolePageReq req);

    /**
     * 为角色分配资源（权限） <p>
     * 覆盖式分配：先清除旧关联，再插入新关联。
     *
     * @param roleId 角色 ID
     * @param resourceIds 资源 ID 列表
     */
    void assignResources(Long roleId, List<Long> resourceIds);

    /**
     * 获取角色已分配的资源 ID 列表
     *
     * @param roleId 角色 ID
     * @return 资源 ID 列表
     */
    List<Long> getRoleResourceIds(Long roleId);

    /**
     * 为用户分配角色 <p>
     * 覆盖式分配：先清除旧关联，再插入新关联。
     *
     * @param userId 用户 ID
     * @param roleIds 角色 ID 列表
     */
    void assignRoles(Long userId, List<Long> roleIds);

    /**
     * 分配用户到角色 <p>
     * 覆盖式分配：先清除该角色的所有用户关联，再插入新关联。
     *
     * @param roleId 角色 ID
     * @param userIds 用户 ID 列表
     */
    void assignRoleUsers(Long roleId, List<Long> userIds);

    /**
     * 获取角色已分配的用户 ID 列表
     *
     * @param roleId 角色 ID
     * @return 用户 ID 列表
     */
    List<Long> getRoleUserIds(Long roleId);
}
