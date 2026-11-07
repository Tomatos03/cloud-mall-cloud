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

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.Result;
import com.cloudmall.coupon.api.model.req.CouponReq;
import com.cloudmall.coupon.api.model.req.RevokeReq;
import com.cloudmall.coupon.api.model.resp.CouponResp;
import com.cloudmall.coupon.api.model.resp.CouponStatsResp;
import com.cloudmall.coupon.application.CouponTemplateApp;
import com.cloudmall.coupon.model.req.CouponPageReq;
import com.cloudmall.coupon.service.ICouponStatsService;
import com.cloudmall.coupon.service.ICouponTemplateService;
import com.cloudmall.coupon.service.IUserCouponService;

/**
 * 优惠券运营端接口
 * <p>
 * 运营可管理全部平台券和店铺券，核心规则仍遵循发布后冻结。
 * 网关 RBAC 拦截非运营访问，服务层二次校验运营角色。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/operator/coupons")
public class OperatorCouponController {

    private final ICouponTemplateService couponTemplateService;
    private final CouponTemplateApp couponTemplateApp;
    private final ICouponStatsService couponStatsService;
    private final IUserCouponService userCouponService;

    /**
     * 分页查询全部模板（平台券与店铺券）。
     *
     * @param req 分页请求
     * @return 模板分页
     */
    @GetMapping
    public Result<IPage<CouponResp>> page(CouponPageReq req) {
        return Result.success(couponTemplateService.pageAll(req));
    }

    /**
     * 创建平台券或店铺券草稿。
     *
     * @param req 创建请求
     * @return 新模板 ID
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Valid CouponReq req) {
        return Result.success(couponTemplateApp.createDraft(req));
    }

    /**
     * 编辑模板草稿（仅草稿可编辑）。
     *
     * @param id 模板 ID
     * @param req 编辑请求
     * @return 处理结果
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid CouponReq req) {
        couponTemplateApp.updateDraft(id, req);
        return Result.success();
    }

    /**
     * 发布模板。
     *
     * @param id 模板 ID
     * @return 处理结果
     */
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        couponTemplateService.publish(id);
        return Result.success();
    }

    /**
     * 暂停发放。
     *
     * @param id 模板 ID
     * @return 处理结果
     */
    @PostMapping("/{id}/pause")
    public Result<Void> pause(@PathVariable Long id) {
        couponTemplateService.pause(id);
        return Result.success();
    }

    /**
     * 结束发放。
     *
     * @param id 模板 ID
     * @return 处理结果
     */
    @PostMapping("/{id}/end")
    public Result<Void> end(@PathVariable Long id) {
        couponTemplateService.end(id);
        return Result.success();
    }

    /**
     * 删除未发放草稿。
     *
     * @param id 模板 ID
     * @return 处理结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        couponTemplateService.deleteDraft(id);
        return Result.success();
    }

    /**
     * 紧急撤销模板下可撤销用户券。
     *
     * @param id 模板 ID
     * @param req 撤销请求
     * @return 撤销数量
     */
    @PostMapping("/{id}/revoke")
    public Result<Integer> revoke(@PathVariable Long id, @RequestBody @Valid RevokeReq req) {
        int revoked = userCouponService.revokeByCoupon(id, UserContextHolder.getUserId(), req.getReason());
        return Result.success(revoked);
    }

    /**
     * 查看模板统计。
     *
     * @param id 模板 ID
     * @return 模板统计
     */
    @GetMapping("/{id}/stats")
    public Result<CouponStatsResp> stats(@PathVariable Long id) {
        return Result.success(couponStatsService.statsByCoupon(id));
    }

    /**
     * 全局统计概览。
     *
     * @return 全局统计
     */
    @GetMapping("/stats/overview")
    public Result<CouponStatsResp> overview() {
        return Result.success(couponStatsService.overview());
    }

}
