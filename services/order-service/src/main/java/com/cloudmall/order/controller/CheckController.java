package com.cloudmall.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.result.Result;
import com.cloudmall.coupon.api.model.resp.CouponOrderCalcResp;
import com.cloudmall.order.api.enums.CheckoutTypeEnum;
import com.cloudmall.order.api.model.req.CheckoutReq;
import com.cloudmall.order.api.model.req.PreviewCheckoutReq;
import com.cloudmall.order.application.checkout.CheckoutApp;

/**
 * 结算接口
 * <p>
 * 提供统一下单入口与下单前价格预览；userId 一律从用户上下文获取，不信任请求参数。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/checkouts")
public class CheckController {

    private final CheckoutApp checkoutApp;

    /**
     * 创建订单（按路径参数中的结算类型分发购物车结算/正常结算/秒杀结算）。
     *
     * @param type 结算类型
     * @param req 下单请求
     * @return 订单编号
     */
    @PostMapping("/{type}")
    public Result<String> checkout(
        @PathVariable("type")
        CheckoutTypeEnum type,
        @RequestBody
        @Valid
        CheckoutReq req
    ) {
        Long userId = UserContextHolder.getUserId();
        return Result.success(checkoutApp.checkout(type, userId, req));
    }

    /**
     * 下单前价格预览（无副作用）。
     *
     * @param req 预览请求（只需商品 SKU 与数量）
     * @return 三级价格明细 + 优惠券使用明细 + 推荐组合
     */
    @GetMapping("/preview")
    public Result<CouponOrderCalcResp> preview(@RequestBody @Valid PreviewCheckoutReq req) {
        return Result.success(checkoutApp.preview(req));
    }
}
