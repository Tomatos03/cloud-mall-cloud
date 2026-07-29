package com.cloudmall.auth.service;

import java.util.List;

import com.cloudmall.auth.model.req.ResourceReq;
import com.cloudmall.auth.model.resp.ResourceNodeResp;
import com.cloudmall.auth.model.resp.ResourceResp;

/**
 * 资源权限统一接口
 * <p>
 * 涵盖资源（菜单/目录/按钮）的增删改查、树形结构管理，
 * 以及用户维度的角色/权限查询与判定。
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
public interface IResourceService {

    /**
     * 获取用户的角色编码列表
     *
     * @param userId 用户 ID
     * @return 角色编码列表（如 ["admin", "merchant"]），不会 null
     */
    List<String> getRoleCodes(Long userId);

    /**
     * 获取用户的权限编码列表
     *
     * @param userId 用户 ID
     * @return 权限编码列表（如 ["goods:create", "order:view"]），不会 null
     */
    List<String> getPermissionCodes(Long userId);

    /**
     * 判断用户是否拥有指定角色
     *
     * @param userId   用户 ID
     * @param roleCode 角色编码
     * @return true 拥有该角色
     */
    boolean hasRole(Long userId, String roleCode);

    /**
     * 判断用户是否拥有指定权限
     *
     * @param userId         用户 ID
     * @param permissionCode 权限编码（如 "goods:create"）
     * @return true 拥有该权限
     */
    boolean hasPermission(Long userId, String permissionCode);

    /**
     * 创建资源
     *
     * @param req 资源创建参数
     * @return 新资源 ID
     */
    Long createResource(ResourceReq req);

    /**
     * 更新资源
     * <p>
     * 根据 id 定位资源，其余非 null 字段将被更新，null 字段保持不变。
     *
     * @param id  资源 ID
     * @param req 待更新的资源字段
     */
    void updateResource(Long id, ResourceReq req);

    /**
     * 删除资源
     *
     * @param resourceId 资源 ID
     */
    void deleteResource(Long resourceId);

    /**
     * 根据 ID 查询资源
     *
     * @param resourceId 资源 ID
     * @return 资源信息
     */
    ResourceResp getResource(Long resourceId);

    /**
     * 查询资源列表
     *
     * @param type 资源类型（menu / catalog / layout / button，null 返回全部）
     * @return 资源列表
     */
    List<ResourceResp> listResources(String type);

    /**
     * 获取完整资源树（按 parentId 构建层级，不进行权限过滤）
     * <p>
     * 仅返回类型为 menu / catalog / layout 的 UI 资源，排除按钮类型。
     * 与 {@link #getResourceTree()} 不同，此方法返回全部 UI 资源，
     * 不根据当前用户权限进行裁剪。用于资源管理后台的全量展示。
     *
     * @return 完整资源树的根节点列表（children 引用子节点）
     */
    List<ResourceNodeResp> getFullResourceTree();
}
