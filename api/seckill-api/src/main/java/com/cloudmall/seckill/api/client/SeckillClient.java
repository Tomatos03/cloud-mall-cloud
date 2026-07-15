package com.cloudmall.seckill.api.client;

import com.cloudmall.common.entity.Result;
import com.cloudmall.seckill.api.response.ActivityResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "seckill-service", path = "/seckill")
public interface SeckillClient {
    @GetMapping("/activity/{id}")
    Result<ActivityResp> getActivity(@PathVariable("id") Long id);

    @PostMapping("/goods/verify")
    Result<Boolean> verifyGoods(@RequestParam("activityId") Long activityId, @RequestParam("goodsId") Long goodsId);
}
