package com.cloudmall.auth.controller;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.auth.model.req.RolePageReq;
import com.cloudmall.auth.model.req.RoleReq;
import com.cloudmall.auth.model.resp.RoleResp;
import com.cloudmall.auth.service.IRoleService;
import com.cloudmall.common.model.Result;

/**
 * 角色管理控制器
 * <p>
 * 提供角色的增删改查以及角色-用户、角色-资源的关联管理。
 *
 * @author : Tomatos
 * @date : 2026/7/29
 */
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final IRoleService roleService;

    /**
     * 创建角色
     */
    @PostMapping
    public Result<Long> createRole(@RequestBody RoleReq req) {
        return Result.success(roleService.createRole(
                req.getName(),
                req.getDescription(),
                Boolean.TRUE.equals(req.getEnabled())
        ));
    }

    /**
     * 更新角色
     * <p>
     * 仅传入需要更新的字段即可，未传的字段保持不变。
     * 支持字段：name、description、enabled（布尔值）。
     */
    @PutMapping("/{id}")
    public Result<Void> updateRole(
            @PathVariable Long id,
            @RequestBody RoleReq req
    ) {
        roleService.updateRole(id, req);
        return Result.success();
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    /**
     * 查询角色
     */
    @GetMapping("/{id}")
    public Result<RoleResp> getRole(@PathVariable Long id) {
        return Result.success(roleService.getRole(id));
    }

    /**
     * 分页查询角色
     */
    @GetMapping
    public Result<IPage<RoleResp>> pageRoles(RolePageReq req) {
        return Result.success(roleService.pageRoles(req));
    }

    /**
     * 覆盖分配资源到角色
     */
    @PutMapping("/{id}/resources")
    public Result<Void> assignResources(
            @PathVariable Long id,
            @RequestBody List<Long> resourceIds
    ) {
        roleService.assignResources(id, resourceIds);
        return Result.success();
    }

    /**
     * 角色的资源 ID 列表
     */
    @GetMapping("/{id}/resources")
    public Result<List<Long>> getRoleResourceIds(@PathVariable Long id) {
        return Result.success(roleService.getRoleResourceIds(id));
    }

    /**
     * 覆盖分配用户到角色
     */
    @PutMapping("/{id}/users")
    public Result<Void> assignUsers(
            @PathVariable Long id,
            @RequestBody List<Long> userIds
    ) {
        roleService.assignRoleUsers(id, userIds);
        return Result.success();
    }

    /**
     * 角色的用户 ID 列表
     */
    @GetMapping("/{id}/users")
    public Result<List<Long>> getRoleUserIds(@PathVariable Long id) {
        return Result.success(roleService.getRoleUserIds(id));
    }
}
