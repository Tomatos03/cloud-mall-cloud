package com.cloudmall.seckill.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.model.Result;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.seckill.api.model.req.SeckillPurchaseReq;
import com.cloudmall.seckill.api.model.resp.ActivityDetailResp;
import com.cloudmall.seckill.api.model.resp.ActivityResp;
import com.cloudmall.seckill.api.model.resp.PurchaseResp;
import com.cloudmall.seckill.api.model.resp.SeckillOrderResp;
import com.cloudmall.seckill.api.model.resp.SeckillProductResp;
import com.cloudmall.seckill.api.model.resp.SeckillSessionResp;
import com.cloudmall.seckill.application.SeckillOrderApp;
import com.cloudmall.seckill.application.SeckillPlainOrderApp;
import com.cloudmall.seckill.service.ISeckillActivityService;
import com.cloudmall.seckill.service.ISeckillOrderService;
import com.cloudmall.seckill.service.ISeckillProductService;

/**
 * 秒杀用户端控制器
 * <p>
 * 活动列表/详情与秒杀商品详情免登录（gateway 白名单）；抢购与我的秒杀单需登录。
 * 本地查询走 service，抢购编排走 {@link SeckillOrderApp}。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/seckill")
public class SeckillUserController {

    private final SeckillOrderApp seckillOrderApp;
    private final SeckillPlainOrderApp seckillPlainOrderApp;
    private final ISeckillActivityService activityService;
    private final ISeckillProductService productService;
    private final ISeckillOrderService orderService;

    /**
     * 进行中活动列表（用户端只显示 RUNNING，免登录）。
     *
     * @return 活动列表
     */
    @GetMapping("/activities")
    public Result<List<ActivityResp>> listRunningActivities() {
        return Result.success(activityService.listRunningActivities());
    }

    /**
     * 活动详情（含已审核商品，免登录）。
     *
     * @param id 活动 ID
     * @return 活动详情
     */
    @GetMapping("/activities/{id}")
    public Result<ActivityDetailResp> getActivityDetail(@PathVariable Long id) {
        return Result.success(activityService.getActivityDetail(id));
    }

    /**
     * 当天全部整点场次（固定 24 场，含未开放场次，免登录）。
     *
     * @return 当天 24 个整点场次
     */
    @GetMapping("/activities/today")
    public Result<List<SeckillSessionResp>> listTodaySessions() {
        return Result.success(activityService.listTodaySessions());
    }

    /**
     * 当前整点场次的进行中活动（含已审核商品，免登录）。
     *
     * @return 活动详情，当前无进行中场次时返回 null
     */
    @GetMapping("/activities/current-hour")
    public Result<ActivityDetailResp> getCurrentHourActivity() {
        return Result.success(activityService.getCurrentHourActivity());
    }

    /**
     * 秒杀商品详情（仅已审核通过，免登录）。
     *
     * @param id 秒杀商品 ID
     * @return 秒杀商品
     */
    @GetMapping("/products/{id}")
    public Result<SeckillProductResp> getApprovedById(@PathVariable Long id) {
        return Result.success(productService.getApprovedById(id));
    }

    /**
     * 抢购（登录态取 userId，立即返回受理结果）。
     *
     * @param req 抢购请求
     * @return 受理结果
     */
    @PostMapping("/purchase")
    public Result<PurchaseResp> purchase(@Valid @RequestBody SeckillPurchaseReq req) {
        return Result.success(seckillOrderApp.purchase(req));
    }

    /**
     * 普通流程抢购（同步 DB 建单，QPS 对比实验用）。
     *
     * @param req 抢购请求
     * @return 受理结果（真实订单号）
     */
    @PostMapping("/plain/purchase")
    public Result<PurchaseResp> purchasePlain(@Valid @RequestBody SeckillPurchaseReq req) {
        return Result.success(seckillPlainOrderApp.purchase(req));
    }

    /**
     * 我的秒杀单列表。
     *
     * @return 秒杀单列表
     */
    @GetMapping("/orders")
    public Result<List<SeckillOrderResp>> listMyOrders() {
        Long userId = UserContextHolder.getUserId();
        Asserts.notNull(userId, BizErrorEnum.USER_NOT_AUTHENTICATED);
        return Result.success(orderService.listByUserId(userId));
    }
}
