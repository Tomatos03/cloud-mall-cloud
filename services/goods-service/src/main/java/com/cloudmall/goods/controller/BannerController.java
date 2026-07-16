package com.cloudmall.goods.controller;

import com.cloudmall.common.entity.Result;
import com.cloudmall.goods.api.response.BannerResp;
import com.cloudmall.goods.service.IBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/banner")
@RequiredArgsConstructor
public class BannerController {

    private final IBannerService bannerService;

    @GetMapping
    public Result<List<BannerResp>> list() {
        return Result.success(bannerService.listActive());
    }
}
