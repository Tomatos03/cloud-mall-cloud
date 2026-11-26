package com.cloudmall.cart.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.cart.api.model.resp.CartSelectedItemResp;
import com.cloudmall.cart.service.ICartService;
import com.cloudmall.common.model.result.Result;

/**
 * 购物车内部接口
 * <p>
 * 内部端点统一使用 /internal/cart，仅供服务间 Feign 调用，不加入网关对外路由。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/cart")
public class InternalCartController {

    private final ICartService cartService;

    /**
     * 查询当前用户已选中的购物车项（下单步骤直读数据库，绕过缓存）。
     *
     * @param userId 用户 ID
     * @return 已选中购物车项列表
     */
    @GetMapping("/selected-items")
    public Result<List<CartSelectedItemResp>> listSelectedItems(@RequestParam("userId") Long userId) {
        return Result.success(cartService.listSelectedItems(userId));
    }

    /**
     * 删除用户购物车中的指定 SKU（下单成功后清理，幂等）。
     *
     * @param userId 用户 ID
     * @param skuIds SKU ID 列表
     * @return 处理结果
     */
    @DeleteMapping("/items")
    public Result<Void> removeItems(@RequestParam("userId") Long userId, @RequestBody List<Long> skuIds) {
        cartService.removeItems(userId, skuIds);
        return Result.success();
    }
}
