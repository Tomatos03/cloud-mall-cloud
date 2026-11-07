package com.cloudmall.coupon.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.coupon.api.model.resp.CouponOperationLogResp;
import com.cloudmall.coupon.model.req.CouponOperationLogPageReq;
import com.cloudmall.coupon.service.ICouponTemplateService;

/**
 * 模板操作审计接口（运营端）
 * <p>
 * 查询模板创建、编辑、发布、暂停、结束、删除、撤销等操作流水。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/operator")
public class OperatorOperationLogController {

    private final ICouponTemplateService couponTemplateService;

    /**
     * 分页查询模板操作审计流水。
     *
     * @param req 分页请求
     * @return 操作流水分页
     */
    @GetMapping("/coupon-operation-logs")
    public Result<IPage<CouponOperationLogResp>> operationLogs(CouponOperationLogPageReq req) {
        return Result.success(couponTemplateService.pageOperationLogs(req));
    }
}
