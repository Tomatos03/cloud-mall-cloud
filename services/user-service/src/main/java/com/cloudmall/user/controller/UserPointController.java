package com.cloudmall.user.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.Result;
import com.cloudmall.user.api.model.req.PointExchangeReq;
import com.cloudmall.user.api.model.resp.PointExchangeItemResp;
import com.cloudmall.user.api.model.resp.PointExchangeResp;
import com.cloudmall.user.api.model.resp.PointTaskResp;
import com.cloudmall.user.api.model.resp.SignInResp;
import com.cloudmall.user.api.model.resp.SignInStatusResp;
import com.cloudmall.user.api.model.resp.UserPointLogResp;
import com.cloudmall.user.api.model.resp.UserPointSummaryResp;
import com.cloudmall.user.service.UserPointService;

/**
 * 当前登录用户积分接口。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me/points")
public class UserPointController {

    private final UserPointService userPointService;

    /**
     * 查询积分汇总。
     *
     * @return 积分汇总
     */
    @GetMapping
    public Result<UserPointSummaryResp> getSummary() {
        return Result.success(userPointService.getSummary(UserContextHolder.getUserId()));
    }

    /**
     * 查询积分流水。
     *
     * @param limit 条数上限
     * @return 积分流水
     */
    @GetMapping("/logs")
    public Result<List<UserPointLogResp>> listLogs(
        @RequestParam(value = "limit", defaultValue = "20")
        Integer limit
    ) {
        return Result.success(userPointService.listLogs(UserContextHolder.getUserId(), limit));
    }

    /**
     * 查询今日签到状态。
     *
     * @return 签到状态
     */
    @GetMapping("/sign-in/status")
    public Result<SignInStatusResp> getSignInStatus() {
        return Result.success(userPointService.getSignInStatus(UserContextHolder.getUserId()));
    }

    /**
     * 今日签到。
     *
     * @return 签到结果
     */
    @PostMapping("/sign-in")
    public Result<SignInResp> signIn() {
        return Result.success(userPointService.signIn(UserContextHolder.getUserId()));
    }

    /**
     * 查询积分任务。
     *
     * @return 任务列表
     */
    @GetMapping("/tasks")
    public Result<List<PointTaskResp>> listTasks() {
        return Result.success(userPointService.listTasks(UserContextHolder.getUserId()));
    }

    /**
     * 领取任务奖励。
     *
     * @param code 任务编码
     * @return 任务状态
     */
    @PostMapping("/tasks/{code}/claim")
    public Result<PointTaskResp> claimTask(@PathVariable String code) {
        return Result.success(userPointService.claimTask(UserContextHolder.getUserId(), code));
    }

    /**
     * 查询积分兑换商品。
     *
     * @return 兑换商品列表
     */
    @GetMapping("/exchange/items")
    public Result<List<PointExchangeItemResp>> listExchangeItems() {
        return Result.success(userPointService.listExchangeItems(UserContextHolder.getUserId()));
    }

    /**
     * 积分兑换优惠券。
     *
     * @param req 兑换请求
     * @return 兑换结果
     */
    @PostMapping("/exchange")
    public Result<PointExchangeResp> exchange(@RequestBody @Valid PointExchangeReq req) {
        return Result.success(userPointService.exchange(UserContextHolder.getUserId(), req));
    }
}
