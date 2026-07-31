package com.cloudmall.seckill.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cloudmall.common.model.Result;
import com.cloudmall.seckill.api.response.ActivityResp;

@FeignClient(name = "seckill-service", contextId = "seckillClient", path = "/seckill")
public interface SeckillClient {

    @GetMapping("/activities/{id}")
    Result<ActivityResp> getActivity(@PathVariable("id") Long id);

    @GetMapping("/activities/{activityId}/products/{productId}/verification")
    Result<Boolean> verifyProduct(@PathVariable("activityId") Long activityId, @PathVariable("productId") Long productId);
}
