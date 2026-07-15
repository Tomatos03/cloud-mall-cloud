package com.cloudmall.user.controller;

import com.cloudmall.common.entity.Result;
import com.cloudmall.user.api.response.StoreResp;
import com.cloudmall.user.service.IStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {

    private final IStoreService storeService;

    @GetMapping
    public Result<StoreResp> getByUserId(@RequestParam Long userId) {
        return Result.success(storeService.getByUserId(userId));
    }

    @GetMapping("/{id}")
    public Result<StoreResp> getById(@PathVariable Long id) {
        return Result.success(storeService.getById(id));
    }
}
