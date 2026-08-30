package com.cloudmall.user.api.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cloudmall.user.api.response.UserResp;

/**
 * 用户内部 Feign 客户端
 * <p>
 * 内部接口统一收敛于 InternalUserClient（/internal/user），
 * 仅供服务间 Feign 调用，不加入网关对外路由。
 *
 * @author : Tomatos
 */
@FeignClient(name = "user-service", contextId = "internalUserClient", path = "/internal/user")
public interface InternalUserClient {

    /**
     * 按用户 ID 批量查询用户信息（昵称、头像等展示信息）。
     *
     * @param ids 用户 ID 列表
     * @return 用户信息列表
     */
    @GetMapping("/batch")
    List<UserResp> listByIds(@RequestParam("ids") List<Long> ids);
}
