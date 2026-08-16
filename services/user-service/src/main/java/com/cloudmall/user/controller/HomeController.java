package com.cloudmall.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.user.application.HomeApp;
import com.cloudmall.user.model.resp.HomeResp;

/**
 * 首页聚合控制器
 * <p>
 * 一次请求返回首页全部数据（轮播图、分类、当前整点秒杀活动、分类展示商品），免登录。
 * 各信息块获取失败时由 {@link HomeApp} 降级，不影响其余块。
 *
 * @author : Tomatos
 */
@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeApp homeApp;

    /**
     * 获取首页聚合数据。
     *
     * @return 首页聚合响应
     */
    @GetMapping
    public Result<HomeResp> getHome() {
        return Result.success(homeApp.getHome());
    }
}
