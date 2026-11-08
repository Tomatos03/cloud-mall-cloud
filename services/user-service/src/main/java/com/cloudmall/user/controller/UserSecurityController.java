package com.cloudmall.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.Result;
import com.cloudmall.user.api.model.req.SetPayPasswordReq;
import com.cloudmall.user.api.model.req.SubmitRealNameReq;
import com.cloudmall.user.api.model.req.VerifyPayPasswordReq;
import com.cloudmall.user.api.model.resp.RealNameResp;
import com.cloudmall.user.api.model.resp.UserSecurityResp;
import com.cloudmall.user.service.UserSecurityService;

/**
 * 当前登录用户账户安全接口。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me")
public class UserSecurityController {

    private final UserSecurityService userSecurityService;

    /**
     * 查询账户安全信息。
     *
     * @return 安全信息
     */
    @GetMapping("/security")
    public Result<UserSecurityResp> getSecurity() {
        return Result.success(userSecurityService.getSecurity(UserContextHolder.getUserId()));
    }

    /**
     * 设置或修改支付密码。
     *
     * @param req 请求参数
     * @return 处理结果
     */
    @PutMapping("/payment-password")
    public Result<Void> setPayPassword(@RequestBody @Valid SetPayPasswordReq req) {
        userSecurityService.setPayPassword(UserContextHolder.getUserId(), req);
        return Result.success();
    }

    /**
     * 校验支付密码。
     *
     * @param req 请求参数
     * @return 校验结果
     */
    @PostMapping("/payment-password/verify")
    public Result<Boolean> verifyPayPassword(@RequestBody @Valid VerifyPayPasswordReq req) {
        return Result.success(userSecurityService.verifyPayPassword(UserContextHolder.getUserId(), req));
    }

    /**
     * 提交实名认证。
     *
     * @param req 请求参数
     * @return 实名认证信息
     */
    @PostMapping("/real-name")
    public Result<RealNameResp> submitRealName(@RequestBody @Valid SubmitRealNameReq req) {
        return Result.success(userSecurityService.submitRealName(UserContextHolder.getUserId(), req));
    }

    /**
     * 查询实名认证信息。
     *
     * @return 实名认证信息
     */
    @GetMapping("/real-name")
    public Result<RealNameResp> getRealName() {
        return Result.success(userSecurityService.getRealName(UserContextHolder.getUserId()));
    }
}
