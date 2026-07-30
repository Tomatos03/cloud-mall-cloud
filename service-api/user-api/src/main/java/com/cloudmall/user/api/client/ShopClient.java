package com.cloudmall.user.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.cloudmall.common.model.Result;
import com.cloudmall.user.api.response.ShopResp;

@FeignClient(name = "user-service", path = "/shops")
public interface ShopClient {

    @GetMapping
    Result<ShopResp> getByUserId(@RequestParam("userId") Long userId);

    @GetMapping("/{id}")
    Result<ShopResp> getById(@PathVariable("id") Long id);

    @GetMapping("/current")
    Result<ShopResp> getCurrent();
}
