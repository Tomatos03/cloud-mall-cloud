package com.cloudmall.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.Result;
import com.cloudmall.user.api.model.req.ChangePasswordReq;
import com.cloudmall.user.api.model.req.UpdateUserProfileReq;
import com.cloudmall.user.api.model.req.UserSettingsUpdateReq;
import com.cloudmall.user.api.model.resp.UserProfileResp;
import com.cloudmall.user.api.model.resp.UserSettingsResp;
import com.cloudmall.user.service.UserProfileService;

/**
 * 当前登录用户个人资料与设置接口。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me")
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * 获取当前用户完整资料。
     *
     * @return 用户资料
     */
    @GetMapping("/profile")
    public Result<UserProfileResp> getProfile() {
        return Result.success(userProfileService.getProfile(UserContextHolder.getUserId()));
    }

    /**
     * 更新当前用户个人资料。
     *
     * @param req 更新参数
     * @return 更新后的用户资料
     */
    @PutMapping("/profile")
    public Result<UserProfileResp> updateProfile(@RequestBody @Valid UpdateUserProfileReq req) {
        return Result.success(userProfileService.updateProfile(UserContextHolder.getUserId(), req));
    }

    /**
     * 修改当前用户登录密码。
     *
     * @param req 密码参数
     * @return 处理结果
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody @Valid ChangePasswordReq req) {
        userProfileService.changePassword(UserContextHolder.getUserId(), req);
        return Result.success();
    }

    /**
     * 获取当前用户通知与隐私设置。
     *
     * @return 设置
     */
    @GetMapping("/settings")
    public Result<UserSettingsResp> getSettings() {
        return Result.success(userProfileService.getSettings(UserContextHolder.getUserId()));
    }

    /**
     * 更新当前用户通知与隐私设置。
     *
     * @param req 更新参数
     * @return 更新后的设置
     */
    @PutMapping("/settings")
    public Result<UserSettingsResp> updateSettings(@RequestBody @Valid UserSettingsUpdateReq req) {
        return Result.success(userProfileService.updateSettings(UserContextHolder.getUserId(), req));
    }
}
