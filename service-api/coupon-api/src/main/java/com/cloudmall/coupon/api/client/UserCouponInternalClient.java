package com.cloudmall.coupon.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.coupon.api.request.AutoIssueRegisterReq;

/**
 * 用户券内部协同 Feign 客户端
 * <p>
 * 内部接口，仅供消费服务消费注册事件后调用；不加入网关对外路由。
 * 用户身份显式传入（异步场景无用户上下文）。
 *
 * @author : Tomatos
 */
@FeignClient(name = "coupon-service", contextId = "userCouponInternalClient", path = "/internal/user-coupons")
public interface UserCouponInternalClient {

    /**
     * 注册自动发放新人券（按事件 ID 幂等）。
     *
     * @param req 发券请求
     * @return 处理结果
     */
    @PostMapping("/auto-issue-register")
    void autoIssueForRegister(
            @RequestBody
            AutoIssueRegisterReq req
    );
}
