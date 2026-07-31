package com.cloudmall.seckill.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.seckill.api.request.ActivityCreateReq;
import com.cloudmall.seckill.api.request.ActivityProductAuditReq;
import com.cloudmall.seckill.api.request.ActivityProductSubmitReq;
import com.cloudmall.seckill.api.response.ActivityResp;
import com.cloudmall.seckill.entity.SeckillActivityDO;
import com.cloudmall.seckill.service.ISeckillService;

@RestController
@RequestMapping("/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final ISeckillService seckillService;

    @PostMapping("/activities")
    public Result<Long> createActivity(@RequestBody ActivityCreateReq req) {
        SeckillActivityDO activity = SeckillActivityDO.builder()
                .name(req.getName())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .createUser(req.getCreateUser())
                .build();
        return Result.success(seckillService.createActivity(activity));
    }

    @GetMapping("/activities/{id}")
    public Result<ActivityResp> getActivity(@PathVariable Long id) {
        return Result.success(seckillService.getActivity(id));
    }

    @PostMapping("/activities/{activityId}/products")
    public Result<Void> submitProduct(@PathVariable Long activityId, @RequestBody ActivityProductSubmitReq req) {
        seckillService.submitProduct(activityId, req.getProductId(), req.getSeckillPrice(), req.getStock());
        return Result.success(null);
    }

    @PatchMapping("/activities/{activityId}/products/{productId}")
    public Result<Void> auditProduct(
            @PathVariable Long activityId,
            @PathVariable Long productId,
            @RequestBody ActivityProductAuditReq req
    ) {
        seckillService.auditProduct(productId, req.isApproved());
        return Result.success(null);
    }

    @GetMapping("/activities/{activityId}/products/{productId}/verification")
    public Result<Boolean> verifyProduct(@PathVariable Long activityId, @PathVariable Long productId) {
        return Result.success(seckillService.verifyProduct(activityId, productId));
    }
}
