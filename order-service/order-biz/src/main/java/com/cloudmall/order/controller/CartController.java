package com.cloudmall.order.controller;
import com.cloudmall.common.entity.Result;
import com.cloudmall.order.api.response.CartResponse;
import com.cloudmall.order.service.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController @RequestMapping("/cart") @RequiredArgsConstructor
public class CartController {
    private final ICartService cartService;

    @GetMapping("/list/{userId}")
    public Result<List<CartResponse>> list(@PathVariable Long userId) {
        return Result.success(cartService.listByUser(userId));
    }

    @PostMapping("/add")
    public Result<Long> add(@RequestParam Long userId, @RequestParam Long goodsId, @RequestParam Long skuId,
                             @RequestParam String goodsName, @RequestParam(required = false) String goodsImage,
                             @RequestParam BigDecimal price, @RequestParam Integer quantity) {
        return Result.success(cartService.addItem(userId, goodsId, skuId, goodsName, goodsImage, price, quantity));
    }

    @PutMapping("/quantity")
    public Result<Void> updateQuantity(@RequestParam Long id, @RequestParam Integer quantity) {
        cartService.updateQuantity(id, quantity);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cartService.deleteItem(id);
        return Result.success(null);
    }

    @DeleteMapping("/clear/{userId}")
    public Result<Void> clear(@PathVariable Long userId) {
        cartService.clearByUser(userId);
        return Result.success(null);
    }
}
