package com.cloudmall.user.controller;

import com.cloudmall.auth.api.response.UserInfoResp;
import com.cloudmall.common.entity.Result;
import com.cloudmall.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
