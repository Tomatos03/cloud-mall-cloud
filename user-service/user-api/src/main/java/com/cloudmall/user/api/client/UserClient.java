package com.cloudmall.user.api.client;

import com.cloudmall.common.entity.Result;
import com.cloudmall.user.api.response.AddressResponse;
import com.cloudmall.user.api.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-service", path = "/user")
public interface UserClient {

    @GetMapping("/{id}")
    Result<UserResponse> getUserById(@PathVariable("id") Long id);

    @GetMapping("/address/list")
    Result<List<AddressResponse>> listAddresses(@RequestParam("userId") Long userId);
}
