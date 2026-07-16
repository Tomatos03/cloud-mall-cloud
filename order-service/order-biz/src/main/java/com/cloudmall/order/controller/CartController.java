package com.cloudmall.order.controller;

import com.cloudmall.common.entity.Result;
import com.cloudmall.order.api.request.CartAddReq;
import com.cloudmall.order.api.response.CartResp;
import com.cloudmall.order.service.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;

    @GetMapping
    public Result<List<CartResp>> list(@RequestParam Long userId) {
        return Result.success(cartService.listByUser(userId));
    }

    @PostMapping
    public Result<Long> add(@RequestBody CartAddReq req) {
        return Result.success(cartService.addItem(
                req.getUserId(), req.getGoodsId(), req.getSkuId(),
                req.getGoodsName(), req.getGoodsImage(), req.getPrice(), req.getQuantity()));
    }

    @PutMapping("/{id}")
    public Result<Void> updateQuantity(@PathVariable Long id, @RequestParam Integer quantity) {
        cartService.updateQuantity(id, quantity);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cartService.deleteItem(id);
        return Result.success(null);
    }

    @DeleteMapping
    public Result<Void> clear(@RequestParam Long userId) {
        cartService.clearByUser(userId);
        return Result.success(null);
    }
}
