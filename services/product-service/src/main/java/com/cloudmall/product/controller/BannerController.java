package com.cloudmall.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.product.api.response.BannerResp;
import com.cloudmall.product.model.req.BannerPageReq;
import com.cloudmall.product.model.req.BannerReq;
import com.cloudmall.product.service.IBannerService;

@RestController
@RequestMapping("/banners")
@RequiredArgsConstructor
public class BannerController {

    private final IBannerService bannerService;

    /**
     * 获取轮播图列表。
     *
     * @param req 查询条件
     * @return 轮播图列表
     */
    @GetMapping
    public Result<List<BannerResp>> list(BannerReq req) {
        return Result.success(bannerService.list(req));
    }

    /**
     * 分页查询轮播图。
     *
     * @param req 分页查询条件
     * @return 轮播图分页结果
     */
    @GetMapping("/page")
    public Result<IPage<BannerResp>> page(BannerPageReq req) {
        return Result.success(bannerService.page(req));
    }
}
