package com.cloudmall.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.Result;
import com.cloudmall.user.api.response.StoreResp;
import com.cloudmall.user.model.req.StoreGetReq;
import com.cloudmall.user.service.IStoreService;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {

    private final IStoreService storeService;

    @GetMapping
    public Result<StoreResp> getByUserId(@RequestParam Long userId) {
        StoreGetReq req = StoreGetReq.builder().userId(userId).build();
        return Result.success(storeService.get(req));
    }

    @GetMapping("/{id}")
    public Result<StoreResp> getById(@PathVariable Long id) {
        StoreGetReq req = StoreGetReq.builder().id(id).build();
        return Result.success(storeService.get(req));
    }

    @GetMapping("/current")
    public Result<StoreResp> getCurrent() {
        StoreGetReq req = StoreGetReq.builder()
                                     .userId(UserContextHolder.getUserId())
                                     .build();
        return Result.success(storeService.get(req));
    }
}
