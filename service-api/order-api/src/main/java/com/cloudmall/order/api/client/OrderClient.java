package com.cloudmall.order.api.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.cloudmall.common.model.Result;
import com.cloudmall.order.api.response.OrderResp;

@FeignClient(name = "order-service", contextId = "orderClient", path = "/orders")
public interface OrderClient {

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     */
    @GetMapping("/{id}")
    Result<OrderResp> getById(@PathVariable("id") Long id);

    /**
     * 分页查询数据。
     *
     * @param userId 用户标识
     * @param current 当前页码
     * @param size 页大小
     * @return 分页数据
     */
    @GetMapping
    Result<Page<OrderResp>> page(@RequestParam("userId") Long userId,
                                 @RequestParam(value = "current", defaultValue = "1") long current,
                                 @RequestParam(value = "size", defaultValue = "20") long size);
}