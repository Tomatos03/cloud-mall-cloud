package com.cloudmall.auth.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cloudmall.auth.api.response.UserInfoResp;

/**
 * 用户信息 Feign 客户端
 * <p>
 * 供 IM 等业务服务按用户 ID 获取昵称、头像等展示信息。
 * 对应 auth-service {@code /users/{id}} 接口。
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@FeignClient(name = "auth-service", contextId = "userClient", path = "/users")
public interface UserClient {

    /**
     * 获取用户信息。
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    UserInfoResp getById(
            @PathVariable("id")
            Long id
    );
}
