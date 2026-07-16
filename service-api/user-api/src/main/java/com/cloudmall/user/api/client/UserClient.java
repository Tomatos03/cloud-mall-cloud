package com.cloudmall.user.api.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.cloudmall.common.entity.Result;
import com.cloudmall.user.api.response.AddressResp;
import com.cloudmall.user.api.response.UserResp;

@FeignClient(name = "user-service", path = "/users")
public interface UserClient {

    @GetMapping("/{id}")
    Result<UserResp> getUserById(@PathVariable("id") Long id);

    @GetMapping("/address/list")
    Result<List<AddressResp>> listAddresses(@RequestParam("userId") Long userId);
}