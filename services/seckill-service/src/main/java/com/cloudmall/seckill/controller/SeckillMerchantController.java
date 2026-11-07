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
import com.cloudmall.seckill.api.model.req.ActivityProductSubmitReq;
import com.cloudmall.seckill.api.model.resp.ActivityDetailResp;
import com.cloudmall.seckill.api.model.resp.ActivityResp;
import com.cloudmall.seckill.api.model.resp.SeckillProductResp;
import com.cloudmall.seckill.application.SeckillApp;
import com.cloudmall.seckill.service.ISeckillActivityService;
import com.cloudmall.seckill.service.ISeckillProductService;

/**
 * 秒杀商家端控制器
 * <p>
 * 商家发现可报名活动（PUBLISHED）、提交商品、查看自己的提交记录。
 * DRAFT 活动对商家不可见。本地查询走 service，提交编排走 {@link SeckillApp}。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/seckill")
public class SeckillMerchantController {

    private final SeckillApp seckillApp;
    private final ISeckillActivityService activityService;
    private final ISeckillProductService productService;

    /**
     * 可报名活动列表（仅 PUBLISHED 报名期）。
     *
     * @return 活动列表
     */
    @GetMapping("/merchant/activities")
    public Result<List<ActivityResp>> listSignupActivities() {
        return Result.success(activityService.listSignupActivities());
    }

    /**
     * 报名活动详情（仅 PUBLISHED，含已审商品）。
     *
     * @param id 活动 ID
     * @return 活动详情
     */
    @GetMapping("/merchant/activities/{id}")
    public Result<ActivityDetailResp> getSignupActivity(@PathVariable Long id) {
        return Result.success(activityService.getSignupActivity(id));
    }

    /**
     * 提交商品参加秒杀（仅 PUBLISHED 且未开始）。
     *
     * @param activityId 活动 ID
     * @param req 提交请求
     * @return 新秒杀商品 ID
     */
    @PostMapping("/activities/{activityId}/products")
    public Result<Long> submitProduct(
        @PathVariable
        Long activityId,
        @Valid
        @RequestBody
        ActivityProductSubmitReq req
    ) {
        return Result.success(seckillApp.submitProduct(activityId, req));
    }

    /**
     * 我的提交列表（按当前商家店铺筛选）。
     *
     * @return 秒杀商品列表
     */
    @GetMapping("/merchant/products")
    public Result<List<SeckillProductResp>> listMyProducts() {
        Long shopId = UserContextHolder.getShopId();
        Asserts.notNull(shopId, BizErrorEnum.MERCHANT_STORE_NOT_FOUND);
        return Result.success(productService.listByShopId(shopId));
    }
}
