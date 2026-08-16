package com.cloudmall.seckill.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.model.Result;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.seckill.api.request.ActivityCreateReq;
import com.cloudmall.seckill.api.request.ActivityProductAuditReq;
import com.cloudmall.seckill.api.request.ActivityUpdateReq;
import com.cloudmall.seckill.api.response.ActivityResp;
import com.cloudmall.seckill.api.response.SeckillProductResp;
import com.cloudmall.seckill.model.req.SeckillActivityPageReq;
import com.cloudmall.seckill.model.req.SeckillProductPageReq;
import com.cloudmall.seckill.service.ISeckillActivityService;
import com.cloudmall.seckill.service.ISeckillProductService;

/**
 * 秒杀运营端控制器
 * <p>
 * 活动创建、编辑、发布、取消与全量查询，商品审核，权限由网关 url-permissions 校验。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/seckill")
public class SeckillOperatorController {

    private final ISeckillActivityService activityService;
    private final ISeckillProductService productService;

    /**
     * 创建活动（DRAFT）。
     *
     * @param req 创建请求
     * @return 新活动 ID
     */
    @PostMapping("/activities")
    public Result<Long> createActivity(@Valid @RequestBody ActivityCreateReq req) {
        Long createUserId = Asserts.requireNonNull(
                UserContextHolder.getUserId(),
                BizError.USER_NOT_AUTHENTICATED
        );
        return Result.success(activityService.createDraft(createUserId, req));
    }

    /**
     * 编辑活动（DRAFT 可改全字段；PUBLISHED 仅文案）。
     *
     * @param id 活动 ID
     * @param req 编辑请求
     * @return 处理结果
     */
    @PatchMapping("/activities/{id}")
    public Result<Void> updateActivity(@PathVariable Long id, @RequestBody ActivityUpdateReq req) {
        activityService.update(id, req);
        return Result.success(null);
    }

    /**
     * 发布活动（DRAFT→PUBLISHED，开放商家报名）。
     *
     * @param id 活动 ID
     * @return 处理结果
     */
    @PostMapping("/activities/{id}/publish")
    public Result<Void> publishActivity(@PathVariable Long id) {
        Long publishUserId = Asserts.requireNonNull(
                UserContextHolder.getUserId(),
                BizError.USER_NOT_AUTHENTICATED
        );
        activityService.publish(id, publishUserId);
        return Result.success(null);
    }

    /**
     * 取消活动（DRAFT/PUBLISHED→CANCELLED，终态）。
     *
     * @param id 活动 ID
     * @return 处理结果
     */
    @PostMapping("/activities/{id}/cancel")
    public Result<Void> cancelActivity(@PathVariable Long id) {
        activityService.cancel(id);
        return Result.success(null);
    }

    /**
     * 活动分页列表（运营端全量，按状态筛选）。
     *
     * @param req 查询请求
     * @return 分页结果
     */
    @GetMapping("/operator/activities")
    public Result<IPage<ActivityResp>> pageActivities(SeckillActivityPageReq req) {
        return Result.success(activityService.pageActivities(req));
    }

    /**
     * 商品分页列表（运营端，按审核状态筛选）。
     *
     * @param req 查询请求
     * @return 分页结果
     */
    @GetMapping("/operator/products")
    public Result<IPage<SeckillProductResp>> pageProducts(SeckillProductPageReq req) {
        return Result.success(productService.pageProducts(req));
    }

    /**
     * 审核秒杀商品（写 cm_audit_record 并同步商品审核状态）。
     *
     * @param activityId 活动 ID
     * @param productId 秒杀商品 ID
     * @param req 审核请求
     * @return 处理结果
     */
    @PatchMapping("/activities/{activityId}/products/{productId}")
    public Result<Void> auditProduct(
        @PathVariable
        Long activityId,
        @PathVariable
        Long productId,
        @RequestBody
        ActivityProductAuditReq req
    ) {
        Long auditorId = Asserts.requireNonNull(
                UserContextHolder.getUserId(),
                BizError.USER_NOT_AUTHENTICATED
        );
        productService.audit(productId, req.isApproved(), req.getAuditRemark(), auditorId);
        return Result.success(null);
    }
}
