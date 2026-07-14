package com.cloudmall.user.controller;

import com.cloudmall.common.entity.Result;
import com.cloudmall.user.api.response.StoreResponse;
import com.cloudmall.user.service.IStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/store")
@RequiredArgsConstructor
public class StoreController {

    private final IStoreService storeService;

    @GetMapping("/userId/{userId}")
    public Result<StoreResponse> getByUserId(@PathVariable Long userId) {
        return Result.success(storeService.getByUserId(userId));
    }

    @GetMapping("/{id}")
    public Result<StoreResponse> getById(@PathVariable Long id) {
        return Result.success(storeService.getById(id));
    }
}
