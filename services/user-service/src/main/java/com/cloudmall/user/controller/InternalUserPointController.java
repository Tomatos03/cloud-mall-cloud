package com.cloudmall.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.user.api.model.req.OrderPointAwardReq;
import com.cloudmall.user.api.model.req.PointChangeReq;
import com.cloudmall.user.api.model.resp.UserPointSummaryResp;
import com.cloudmall.user.service.UserPointService;

/**
 * 用户积分内部接口。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/user/points")
public class InternalUserPointController {

    private final UserPointService userPointService;

    /**
     * 变更用户积分（订单支付、评价等事件调用）。
     *
     * @param req 积分变动请求
     * @return 变更后的积分汇总
     */
    @PostMapping("/change")
    public Result<UserPointSummaryResp> change(@RequestBody @Valid PointChangeReq req) {
        return Result.success(
                userPointService.change(
                        req.getUserId(),
                        req.getChangeAmount(),
                        req.getType(),
                        req.getDescription()
                )
        );
    }

    /**
     * 订单支付成功发放积分。
     *
     * @param req 发放请求
     * @return 处理结果
     */
    @PostMapping("/award-order")
    public Result<Void> awardOrderPoints(@RequestBody @Valid OrderPointAwardReq req) {
        userPointService.awardOrderPoints(req.getUserId(), req.getOrderNo(), req.getAmount());
        return Result.success();
    }

    /**
     * 完成任务事件。
     *
     * @param code 任务编码
     * @param userId 用户 ID
     * @return 处理结果
     */
    @PostMapping("/tasks/{code}/complete")
    public Result<Void> completeTask(@PathVariable String code, @RequestParam Long userId) {
        userPointService.completeTask(userId, code);
        return Result.success();
    }
}
