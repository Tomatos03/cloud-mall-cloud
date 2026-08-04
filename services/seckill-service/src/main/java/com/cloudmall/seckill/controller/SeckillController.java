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
@RequiredArgsConstructor
@RequestMapping("/seckill")
public class SeckillController {

    private final ISeckillService seckillService;

    /**
     * 创建并保存数据。
     *
     * @param req 请求参数
     * @return 处理结果
     */
    @PostMapping("/activities")
    public Result<Long> createActivity(
            @RequestBody
            ActivityCreateReq req
    ) {
        SeckillActivityDO activity = SeckillActivityDO.builder()
                                                      .name(req.getName())
                                                      .startTime(req.getStartTime())
                                                      .endTime(req.getEndTime())
                                                      .createUser(req.getCreateUser())
                                                      .build();
        return Result.success(seckillService.createActivity(activity));
    }

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     */
    @GetMapping("/activities/{id}")
    public Result<ActivityResp> getActivity(
            @PathVariable
            Long id
    ) {
        return Result.success(seckillService.getActivity(id));
    }

    /**
     * 提交商品参加秒杀活动。
     *
     * @param activityId 活动标识
     * @param req 请求参数
     * @return 处理结果
     */
    @PostMapping("/activities/{activityId}/products")
    public Result<Void> submitProduct(
            @PathVariable
            Long activityId,
            @RequestBody
            ActivityProductSubmitReq req
    ) {
        seckillService.submitProduct(activityId, req.getProductId(), req.getSeckillPrice(), req.getStock());
        return Result.success(null);
    }

    /**
     * 审核秒杀活动商品。
     *
     * @param activityId 活动标识
     * @param productId 商品标识
     * @param req 请求参数
     * @return 处理结果
     */
    @PatchMapping("/activities/{activityId}/products/{productId}")
    public Result<Void> auditProduct(
            @PathVariable
            Long activityId,
            @PathVariable
            Long productId,
            @RequestBody
            ActivityProductAuditReq req
    ) {
        seckillService.auditProduct(productId, req.isApproved());
        return Result.success(null);
    }

    /**
     * 校验相关数据。
     *
     * @param activityId 活动标识
     * @param productId 商品标识
     * @return 处理结果
     */
    @GetMapping("/activities/{activityId}/products/{productId}/verification")
    public Result<Boolean> verifyProduct(
            @PathVariable
            Long activityId,
            @PathVariable
            Long productId
    ) {
        return Result.success(seckillService.verifyProduct(activityId, productId));
    }
}
