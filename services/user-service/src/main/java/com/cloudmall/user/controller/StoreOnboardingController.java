package com.cloudmall.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.model.Result;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.user.api.model.req.StoreRegisterReq;
import com.cloudmall.user.api.model.req.StoreReviewReq;
import com.cloudmall.user.api.model.resp.StoreApplicationResp;
import com.cloudmall.user.service.IStoreOnboardingService;

/**
 * 商家入驻接口。
 * <p>
 * 用户查询/提交入驻资料；管理员通过 review 端点完成平台审核。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/shops/onboarding")
public class StoreOnboardingController {

    private final IStoreOnboardingService storeOnboardingService;

    /**
     * 查询当前用户的入驻状态。
     *
     * @return 入驻申请状态
     */
    @GetMapping
    public Result<StoreApplicationResp> status() {
        return Result.success(storeOnboardingService.getStatus(currentUserId()));
    }

    /**
     * 提交或重新提交入驻申请。
     *
     * @param req 入驻资料
     * @return 提交后的申请状态
     */
    @PostMapping
    public Result<StoreApplicationResp> submit(@RequestBody @Valid StoreRegisterReq req) {
        return Result.success(storeOnboardingService.submit(currentUserId(), req));
    }

    /**
     * 审核入驻申请（仅管理员）。
     *
     * @param req 审核请求
     * @return 审核后的申请状态
     */
    @PostMapping("/review")
    public Result<StoreApplicationResp> review(@RequestBody @Valid StoreReviewReq req) {
        Asserts.isTrue(UserContextHolder.isAdmin(), BizErrorEnum.NO_MANAGE_PERMISSION);
        return Result.success(
                storeOnboardingService.review(
                        req.getApplicationId(),
                        Boolean.TRUE.equals(req.getApproved()),
                        req.getRemark(),
                        currentUserId()
                )
        );
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 用户 ID
     */
    private static Long currentUserId() {
        return Asserts.requireNonNull(UserContextHolder.getUserId(), BizErrorEnum.USER_NOT_AUTHENTICATED);
    }
}
