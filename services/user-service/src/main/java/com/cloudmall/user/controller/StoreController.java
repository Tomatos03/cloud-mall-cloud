package com.cloudmall.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.entity.Result;
import com.cloudmall.user.api.response.StoreResp;
import com.cloudmall.user.service.IStoreService;

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
