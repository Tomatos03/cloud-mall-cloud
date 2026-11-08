package com.cloudmall.coupon.api.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.cloudmall.coupon.api.model.resp.CouponResp;

/**
 * 优惠券用户端 Feign 客户端，供 AI 客服查询公开券中心。
 */
@FeignClient(name = "coupon-service", contextId = "couponClient", path = "/coupons")
public interface CouponClient {

    /**
     * 查询公开券中心列表。
     *
     * @return 公开优惠券模板列表
     */
    @GetMapping
    List<CouponResp> listPublic();
}
