package com.cloudmall.user.api.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.cloudmall.common.model.Result;
import com.cloudmall.user.api.response.AddressResp;

@FeignClient(name = "user-service", path = "/address")
public interface AddressClient {
    @GetMapping("/address/list")
    Result<List<AddressResp>> listAddresses(@RequestParam("userId") Long userId);
}
