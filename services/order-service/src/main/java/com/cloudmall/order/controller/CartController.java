package com.cloudmall.order.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.Result;
import com.cloudmall.order.api.request.CartOperatorReq;
import com.cloudmall.order.api.request.CartSelectReq;
import com.cloudmall.order.api.response.CartDetailResp;
import com.cloudmall.order.service.ICartService;

/**
 * 购物车控制器
 *
 * @author : Tomatos
 */
@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;

    /**
     * 添加商品到购物车
     */
    @PostMapping
    public Result<Long> add(@Valid @RequestBody CartOperatorReq req) {
        Long userId = UserContextHolder.getUserId();
        return Result.success(cartService.addToCart(userId, req));
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/sku/{skuId}")
    public Result<Void> updateQuantity(
            @PathVariable Long skuId,
            @RequestParam Integer quantity
    ) {
        CartOperatorReq cartOperatorReq = new CartOperatorReq()
                .skuId(skuId)
                .quantity(quantity);
        cartService.updateQuantity(UserContextHolder.getUserId(), cartOperatorReq);
        return Result.success(null);
    }

    /**
     * 移除购物车中的商品
     */
    @DeleteMapping("/skus")
    public Result<Void> removeItem(@RequestBody List<Long> skuIds) {
        cartService.removeItems(UserContextHolder.getUserId(), skuIds);
        return Result.success(null);
    }

    /**
     * 选中/取消选中指定商品
     */
    @PutMapping("/selection")
    public Result<Void> selectItems(@Valid @RequestBody CartSelectReq req) {
        cartService.selectItems(UserContextHolder.getUserId(), req);
        return Result.success(null);
    }

    /**
     * 全选/全不选
     */
    @PutMapping("/selection/all")
    public Result<Void> selectAll(@RequestParam Boolean selected) {
        cartService.selectAll(UserContextHolder.getUserId(), selected);
        return Result.success(null);
    }

    /**
     * 选中/取消选中指定店铺的所有商品
     */
    @PutMapping("/selection/shop/{shopId}")
    public Result<Void> selectShop(
            @PathVariable @NotNull Long shopId,
            @RequestParam @NotNull Boolean selected
    ) {
        cartService.selectShop(UserContextHolder.getUserId(), shopId, selected);
        return Result.success(null);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping
    public Result<Void> clear() {
        cartService.clear(UserContextHolder.getUserId());
        return Result.success(null);
    }

    /**
     * 获取用户购物车内容
     * <p>
     * 返回按店铺分组的商品列表及全车汇总统计。
     */
    @GetMapping
    public Result<CartDetailResp> getCart() {
        Long userId = UserContextHolder.getUserId();
        return Result.success(cartService.getCart(userId));
    }
}
