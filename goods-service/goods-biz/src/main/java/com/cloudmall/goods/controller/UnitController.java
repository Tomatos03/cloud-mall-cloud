package com.cloudmall.goods.controller;

import com.cloudmall.common.entity.Result;
import com.cloudmall.goods.api.response.UnitResponse;
import com.cloudmall.goods.service.IUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/unit")
@RequiredArgsConstructor
public class UnitController {

    private final IUnitService unitService;

    @GetMapping("/list")
    public Result<List<UnitResponse>> list() {
        return Result.success(unitService.listAll());
    }
}
