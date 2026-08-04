package com.cloudmall.coupon.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.coupon.api.request.CouponReq;
import com.cloudmall.coupon.api.response.CouponResp;
import com.cloudmall.coupon.api.response.CouponStatsResp;
import com.cloudmall.coupon.enums.CouponOwnerTypeEnum;
import com.cloudmall.coupon.model.req.CouponPageReq;
import com.cloudmall.coupon.service.ICouponStatsService;
import com.cloudmall.coupon.service.ICouponTemplateService;

/**
 * 优惠券商家端接口
 * <p>
 * 商家只能管理自己店铺的优惠券：从当前用户解析唯一店铺，
 * 不信任请求中的 shopId，服务层强制店铺归属校验。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/merchant/coupons")
public class MerchantCouponController {

    private final ICouponTemplateService couponTemplateService;
    private final ICouponStatsService couponStatsService;

    /**
     * 分页查询当前商家店铺券。
     *
     * @param req 分页请求
     * @return 店铺券分页
     */
    @GetMapping
    public Result<IPage<CouponResp>> page(CouponPageReq req) {
        return Result.success(couponTemplateService.pageShop(req));
    }

    /**
     * 创建店铺券草稿（强制归属当前店铺）。
     *
     * @param req 创建请求
     * @return 新模板 ID
     */
    @PostMapping
    public Result<Long> create(
            @RequestBody
            @Valid
            CouponReq req
    ) {
        req.setOwnerType(CouponOwnerTypeEnum.SHOP);
        return Result.success(couponTemplateService.createDraft(req));
    }

    /**
     * 编辑店铺券草稿（仅草稿可编辑）。
     *
     * @param id 模板 ID
     * @param req 编辑请求
     * @return 处理结果
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable
            Long id,
            @RequestBody
            @Valid
            CouponReq req
    ) {
        couponTemplateService.updateDraft(id, req);
        return Result.success();
    }

    /**
     * 发布店铺券（直接发布，无审核）。
     *
     * @param id 模板 ID
     * @return 处理结果
     */
    @PostMapping("/{id}/publish")
    public Result<Void> publish(
            @PathVariable
            Long id
    ) {
        couponTemplateService.publish(id);
        return Result.success();
    }

    /**
     * 暂停店铺券发放。
     *
     * @param id 模板 ID
     * @return 处理结果
     */
    @PostMapping("/{id}/pause")
    public Result<Void> pause(
            @PathVariable
            Long id
    ) {
        couponTemplateService.pause(id);
        return Result.success();
    }

    /**
     * 结束店铺券发放。
     *
     * @param id 模板 ID
     * @return 处理结果
     */
    @PostMapping("/{id}/end")
    public Result<Void> end(
            @PathVariable
            Long id
    ) {
        couponTemplateService.end(id);
        return Result.success();
    }

    /**
     * 删除未发放店铺券草稿。
     *
     * @param id 模板 ID
     * @return 处理结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable
            Long id
    ) {
        couponTemplateService.deleteDraft(id);
        return Result.success();
    }

    /**
     * 查看店铺券统计。
     *
     * @param id 模板 ID
     * @return 模板统计
     */
    @GetMapping("/{id}/stats")
    public Result<CouponStatsResp> stats(
            @PathVariable
            Long id
    ) {
        return Result.success(couponStatsService.statsByCoupon(id));
    }
}
