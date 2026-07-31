package com.cloudmall.auth.controller;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.auth.api.request.LoginReq;
import com.cloudmall.auth.api.request.RegisterReq;
import com.cloudmall.auth.api.response.LoginResp;
import com.cloudmall.auth.api.response.UserInfoResp;
import com.cloudmall.auth.model.req.UserPageReq;
import com.cloudmall.auth.model.resp.ResourceNodeResp;
import com.cloudmall.auth.service.IRoleService;
import com.cloudmall.auth.service.IUserService;
import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.Result;

/**
 * 用户管理控制器
 * <p>
 * 提供认证（登录/注册）、用户管理（列表/查询/角色分配）。
 * 用户密码已在数据库实体中通过 @JsonIgnore 隐藏，不进入响应。
 *
 * @author : Tomatos
 * @date : 2026/7/29
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final IRoleService roleService;

    /**
     * 执行用户登录认证。
     *
     * @param request 请求参数
     * @return 处理结果
     */
    @PostMapping("/login")
    public Result<LoginResp> login(@RequestBody @Valid LoginReq request) {
        return Result.success(userService.login(request));
    }

    /**
     * 创建并保存数据。
     *
     * @param request 请求参数
     * @return 处理结果
     */
    @PostMapping("/register")
    public Result<Long> register(@RequestBody @Valid RegisterReq request) {
        return Result.success(userService.register(request));
    }

    /**
     * 分页查询数据。
     *
     * @param req 请求参数
     * @return 分页数据
     */
    @GetMapping
    public Result<IPage<UserInfoResp>> pageUsers(UserPageReq req) {
        return Result.success(userService.pageUserInfos(req));
    }

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     */
    @GetMapping("/{id}")
    public Result<UserInfoResp> getUser(@PathVariable Long id) {
        return Result.success(userService.getUserInfo(id));
    }

    /**
     * 获取当前用户有权限访问的菜单树
     * <p>
     * 返回按 parentId 构建的层级菜单结构，已按当前用户的权限编码过滤。
     * 仅包含 menu / catalog / layout 类型的 UI 节点。
     *
     * @return 菜单树根节点列表
     */
    @GetMapping("/menu")
    public Result<List<ResourceNodeResp>> getMenuTree() {
        return Result.success(userService.getMenuResourceTree());
    }

    /**
     * 更新相关数据。
     *
     * @param roleIds 角色标识列表
     * @return 处理结果
     */
    @PutMapping("/roles")
    public Result<Void> assignRoles(@RequestBody List<Long> roleIds) {
        roleService.assignRoles(UserContextHolder.getUserId(), roleIds);
        return Result.success();
    }

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     */
    @GetMapping("/{id}/roles")
    public Result<List<Long>> getUserRoleIds(@PathVariable Long id) {
        return Result.success(userService.getUserRoleIds(id));
    }
}
