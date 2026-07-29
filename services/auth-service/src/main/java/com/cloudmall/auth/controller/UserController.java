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

    @PostMapping("/login")
    public Result<LoginResp> login(@RequestBody @Valid LoginReq request) {
        return Result.success(userService.login(request));
    }

    @PostMapping("/register")
    public Result<Long> register(@RequestBody @Valid RegisterReq request) {
        return Result.success(userService.register(request));
    }

    @GetMapping
    public Result<IPage<UserInfoResp>> pageUsers(UserPageReq req) {
        return Result.success(userService.pageUserInfos(req));
    }

    @GetMapping("/{id}")
    public Result<UserInfoResp> getUser(@PathVariable Long id) {
        return Result.success(userService.getUserInfo(id));
    }

    @PutMapping("/roles")
    public Result<Void> assignRoles(@RequestBody List<Long> roleIds) {
        roleService.assignRoles(UserContextHolder.getUserId(), roleIds);
        return Result.success();
    }

    @GetMapping("/{id}/roles")
    public Result<List<Long>> getUserRoleIds(@PathVariable Long id) {
        return Result.success(userService.getUserRoleIds(id));
    }
}
