package com.cloudmall.coupon.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.coupon.api.request.AutoIssueRegisterReq;
import com.cloudmall.coupon.service.IUserCouponService;

/**
 * 用户券内部协同接口
 * <p>
 * 仅供消费服务通过 Feign 调用，不加入网关对外路由。
 * 用户身份显式传入（异步场景无用户上下文）。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/user-coupons")
public class UserCouponInternalController {

    private final IUserCouponService userCouponService;

    /**
     * 注册自动发放新人券（按事件 ID 幂等）。
     *
     * @param req 发券请求
     * @return 处理结果
     */
    @PostMapping("/auto-issue-register")
    public Result<Void> autoIssueForRegister(
            @RequestBody
            @Valid
            AutoIssueRegisterReq req
    ) {
        userCouponService.autoIssueForRegister(req.getUserId(), req.getEventId());
        return Result.success();
    }
}
