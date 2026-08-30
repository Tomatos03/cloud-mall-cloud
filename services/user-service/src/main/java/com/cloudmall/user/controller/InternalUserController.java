package com.cloudmall.user.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.user.api.response.UserResp;
import com.cloudmall.user.service.IUserService;

/**
 * 用户内部接口
 * <p>
 * 内部服务端点统一使用 /internal/user，仅供服务间 Feign 调用，不加入网关对外路由。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/user")
public class InternalUserController {

    private final IUserService userService;

    /**
     * 按用户 ID 批量查询用户信息。
     *
     * @param ids 用户 ID 列表
     * @return 用户信息列表
     */
    @GetMapping("/batch")
    public Result<List<UserResp>> listByIds(@RequestParam("ids") List<Long> ids) {
        return Result.success(userService.listByIds(ids));
    }
}
