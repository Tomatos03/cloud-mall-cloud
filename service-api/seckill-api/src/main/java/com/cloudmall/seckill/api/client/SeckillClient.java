package com.cloudmall.seckill.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cloudmall.seckill.api.response.ActivityResp;

@FeignClient(name = "seckill-service", contextId = "seckillClient", path = "/seckill")
public interface SeckillClient {

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     */
    @GetMapping("/activities/{id}")
    ActivityResp getActivity(
            @PathVariable("id")
            Long id
    );

    /**
     * 校验相关数据。
     *
     * @param activityId 活动标识
     * @param productId 商品标识
     * @return 处理结果
     */
    @GetMapping("/activities/{activityId}/products/{productId}/verification")
    Boolean verifyProduct(
            @PathVariable("activityId")
            Long activityId,
            @PathVariable("productId")
            Long productId
    );
}
