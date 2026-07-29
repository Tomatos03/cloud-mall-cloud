package com.cloudmall.goods.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.goods.api.response.UnitResp;
import com.cloudmall.goods.model.req.UnitPageReq;
import com.cloudmall.goods.service.IUnitService;

@RestController
@RequestMapping("/units")
@RequiredArgsConstructor
public class UnitController {

    private final IUnitService unitService;

    @GetMapping
    public Result<IPage<UnitResp>> page(UnitPageReq req) {
        return Result.success(unitService.page(req));
    }
}
