package com.cloudmall.seckill.controller;

import com.cloudmall.common.entity.Result;
import com.cloudmall.seckill.api.request.ActivityCreateReq;
import com.cloudmall.seckill.api.request.ActivityGoodsAuditReq;
import com.cloudmall.seckill.api.request.ActivityGoodsSubmitReq;
import com.cloudmall.seckill.api.response.ActivityResp;
import com.cloudmall.seckill.entity.SeckillActivityDO;
import com.cloudmall.seckill.service.ISeckillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final ISeckillService seckillService;

    @PostMapping("/activities")
    public Result<Long> createActivity(@RequestBody ActivityCreateReq req) {
        SeckillActivityDO activity = new SeckillActivityDO();
        activity.setName(req.getName());
        activity.setStartTime(req.getStartTime());
        activity.setEndTime(req.getEndTime());
        activity.setCreateUser(req.getCreateUser());
        return Result.success(seckillService.createActivity(activity));
    }

    @GetMapping("/activities/{id}")
    public Result<ActivityResp> getActivity(@PathVariable Long id) {
        return Result.success(seckillService.getActivity(id));
    }

    @PostMapping("/activities/{activityId}/goods")
    public Result<Void> submitGoods(@PathVariable Long activityId, @RequestBody ActivityGoodsSubmitReq req) {
        seckillService.submitGoods(activityId, req.getGoodsId(), req.getSeckillPrice(), req.getStock());
        return Result.success(null);
    }

    @PatchMapping("/activities/{activityId}/goods/{goodsId}")
    public Result<Void> auditGoods(@PathVariable Long activityId, @PathVariable Long goodsId,
                                    @RequestBody ActivityGoodsAuditReq req) {
        seckillService.auditGoods(goodsId, req.isApproved());
        return Result.success(null);
    }

    @GetMapping("/activities/{activityId}/goods/{goodsId}/verification")
    public Result<Boolean> verifyGoods(@PathVariable Long activityId, @PathVariable Long goodsId) {
        return Result.success(seckillService.verifyGoods(activityId, goodsId));
    }
}
