package com.cloudmall.seckill.controller;

import com.cloudmall.common.entity.Result;
import com.cloudmall.seckill.api.response.ActivityResp;
import com.cloudmall.seckill.entity.SeckillActivityDO;
import com.cloudmall.seckill.service.ISeckillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/seckill")
@RequiredArgsConstructor
public class SeckillController {
    private final ISeckillService seckillService;

    @PostMapping("/activity")
    public Result<Long> createActivity(@RequestBody SeckillActivityDO activity) {
        return Result.success(seckillService.createActivity(activity));
    }

    @GetMapping("/activity/{id}")
    public Result<ActivityResp> getActivity(@PathVariable Long id) {
        return Result.success(seckillService.getActivity(id));
    }

    @PostMapping("/goods/submit")
    public Result<Void> submitGoods(@RequestParam Long activityId, @RequestParam Long goodsId,
                                     @RequestParam BigDecimal seckillPrice, @RequestParam Integer stock) {
        seckillService.submitGoods(activityId, goodsId, seckillPrice, stock);
        return Result.success(null);
    }

    @PostMapping("/goods/audit")
    public Result<Void> auditGoods(@RequestParam Long goodsId, @RequestParam boolean approved) {
        seckillService.auditGoods(goodsId, approved);
        return Result.success(null);
    }

    @PostMapping("/goods/verify")
    public Result<Boolean> verifyGoods(@RequestParam Long activityId, @RequestParam Long goodsId) {
        return Result.success(seckillService.verifyGoods(activityId, goodsId));
    }
}
