package com.cloudmall.user.api.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.cloudmall.user.api.model.req.OrderPointAwardReq;
import com.cloudmall.user.api.model.resp.AddressResp;
import com.cloudmall.user.api.model.resp.UserResp;

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

    /**
     * 按地址 ID 查询收货地址（归属校验：不存在或不属于该用户时返回 null）。
     *
     * @param id 地址 ID
     * @param userId 归属用户 ID
     * @return 地址响应，不存在或不属于该用户时为 null
     */
    @GetMapping("/addresses/{id}")
    AddressResp getAddressById(@PathVariable("id") Long id, @RequestParam("userId") Long userId);

    /**
     * 完成积分任务事件。
     *
     * @param code 任务编码
     * @param userId 用户 ID
     */
    @PostMapping("/points/tasks/{code}/complete")
    void completePointTask(@PathVariable("code") String code, @RequestParam("userId") Long userId);

    /**
     * 订单支付成功发放积分。
     *
     * @param req 发放请求
     */
    @PostMapping("/points/award-order")
    void awardOrderPoints(@RequestBody OrderPointAwardReq req);
}
