package com.cloudmall.user.api.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.cloudmall.user.api.response.ShopResp;

@FeignClient(name = "user-service", contextId = "shopClient", path = "/shops")
public interface ShopClient {

    /**
     * 获取相关数据。
     *
     * @param userId 用户标识
     * @return 处理结果
     */
    @GetMapping
    ShopResp getByUserId(
            @RequestParam("userId")
            Long userId
    );

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     */
    @GetMapping("/{id}")
    ShopResp getById(
            @PathVariable("id")
            Long id
    );

    /**
     * 获取相关数据。
     *
     * @return 处理结果
     */
    @GetMapping("/current")
    ShopResp getCurrent();

    /**
     * 批量查询店铺信息（名称、Logo 等）。 <p>
     * 供购物车、订单等场景按店铺分组时一次取齐，避免逐条调用。
     *
     * @param shopIds 店铺 ID 列表
     * @return 店铺信息列表
     */
    @PostMapping("/batch-query")
    List<ShopResp> listByIds(
            @RequestBody
            List<Long> shopIds
    );
}
