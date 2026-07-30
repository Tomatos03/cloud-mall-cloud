package com.cloudmall.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.Result;
import com.cloudmall.user.api.response.ShopResp;
import com.cloudmall.user.model.req.ShopGetReq;
import com.cloudmall.user.service.IShopService;

@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
public class ShopController {

    private final IShopService shopService;

    @GetMapping
    public Result<ShopResp> getByUserId(@RequestParam Long userId) {
        ShopGetReq req = ShopGetReq.builder().userId(userId).build();
        return Result.success(shopService.get(req));
    }

    @GetMapping("/{id}")
    public Result<ShopResp> getById(@PathVariable Long id) {
        ShopGetReq req = ShopGetReq.builder().id(id).build();
        return Result.success(shopService.get(req));
    }

    @GetMapping("/current")
    public Result<ShopResp> getCurrent() {
        ShopGetReq req = ShopGetReq.builder()
                                     .userId(UserContextHolder.getUserId())
                                     .build();
        return Result.success(shopService.get(req));
    }
}
