package com.cloudmall.goods.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.goods.api.response.BannerResp;
import com.cloudmall.goods.model.req.BannerPageReq;
import com.cloudmall.goods.model.req.BannerReq;
import com.cloudmall.goods.service.IBannerService;

@RestController
@RequestMapping("/banners")
@RequiredArgsConstructor
public class BannerController {

    private final IBannerService bannerService;

    @GetMapping
    public Result<List<BannerResp>> list(BannerReq req) {
        return Result.success(bannerService.list(req));
    }

    @GetMapping("/page")
    public Result<IPage<BannerResp>> page(BannerPageReq req) {
        return Result.success(bannerService.page(req));
    }
}
