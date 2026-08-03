package com.cloudmall.user.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.Result;
import com.cloudmall.user.api.response.ShopResp;
import com.cloudmall.user.model.req.ShopGetReq;
import com.cloudmall.user.service.IShopService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shops")
public class ShopController {

    private final IShopService shopService;

    /**
     * 获取相关数据。
     *
     * @param userId 用户标识
     * @return 处理结果
     */
    @GetMapping
    public Result<ShopResp> getByUserId(
            @RequestParam
            Long userId
    ) {
        ShopGetReq req = ShopGetReq.builder()
                                   .userId(userId)
                                   .build();
        return Result.success(shopService.get(req));
    }

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     */
    @GetMapping("/{id}")
    public Result<ShopResp> getById(
            @PathVariable
            Long id
    ) {
        ShopGetReq req = ShopGetReq.builder()
                                   .id(id)
                                   .build();
        return Result.success(shopService.get(req));
    }

    /**
     * 获取相关数据。
     *
     * @return 处理结果
     */
    @GetMapping("/current")
    public Result<ShopResp> getCurrent() {
        ShopGetReq req = ShopGetReq.builder()
                                   .userId(UserContextHolder.getUserId())
                                   .build();
        return Result.success(shopService.get(req));
    }

    /**
     * 批量查询店铺信息。
     *
     * @param shopIds 店铺 ID 列表
     * @return 店铺信息列表
     */
    @PostMapping("/batch-query")
    public Result<List<ShopResp>> listByIds(
            @RequestBody
            List<Long> shopIds
    ) {
        return Result.success(shopService.listByIds(shopIds));
    }
}
