package com.cloudmall.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.user.api.model.req.RealNameReviewReq;
import com.cloudmall.user.service.UserSecurityService;

/**
 * 用户账户安全内部接口。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/user/security")
public class InternalUserSecurityController {

    private final UserSecurityService userSecurityService;

    /**
     * 审核实名认证。
     *
     * @param req 审核请求
     * @return 处理结果
     */
    @PostMapping("/real-name/review")
    public Result<Void> reviewRealName(@RequestBody @Valid RealNameReviewReq req) {
        userSecurityService.reviewRealName(req);
        return Result.success();
    }
}
