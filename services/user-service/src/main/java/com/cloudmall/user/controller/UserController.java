package com.cloudmall.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.auth.api.response.UserInfoResp;
import com.cloudmall.common.entity.Result;
import com.cloudmall.user.service.IUserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @GetMapping("/{id}")
    public Result<UserInfoResp> getUserById(@PathVariable("id") Long id) {
        return Result.success(userService.getUserInfo(id));
    }
}
