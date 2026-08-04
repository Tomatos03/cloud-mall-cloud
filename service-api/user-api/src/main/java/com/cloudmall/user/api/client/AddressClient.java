package com.cloudmall.user.api.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cloudmall.user.api.response.AddressResp;

@FeignClient(name = "user-service", contextId = "addressClient", path = "/addresses")
public interface AddressClient {

    /**
     * 查询数据列表。
     *
     * @param userId 用户标识
     * @return 处理结果
     */
    @GetMapping
    List<AddressResp> listAddresses(
            @RequestParam("userId")
            Long userId
    );
}
