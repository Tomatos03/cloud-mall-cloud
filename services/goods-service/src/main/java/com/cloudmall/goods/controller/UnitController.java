package com.cloudmall.goods.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.entity.Result;
import com.cloudmall.goods.api.response.UnitResp;
import com.cloudmall.goods.service.IUnitService;

@RestController
@RequestMapping("/units")
@RequiredArgsConstructor
public class UnitController {

    private final IUnitService unitService;

    @GetMapping
    public Result<List<UnitResp>> list() {
        return Result.success(unitService.listAll());
    }
}
