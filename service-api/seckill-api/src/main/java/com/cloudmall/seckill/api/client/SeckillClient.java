package com.cloudmall.seckill.api.client;

import com.cloudmall.common.entity.Result;
import com.cloudmall.seckill.api.response.ActivityResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "seckill-service", path = "/seckill")
public interface SeckillClient {

    @GetMapping("/activities/{id}")
    Result<ActivityResp> getActivity(@PathVariable("id") Long id);

    @GetMapping("/activities/{activityId}/goods/{goodsId}/verification")
    Result<Boolean> verifyGoods(@PathVariable("activityId") Long activityId, @PathVariable("goodsId") Long goodsId);
}
